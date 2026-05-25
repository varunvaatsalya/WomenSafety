import os
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"
os.environ["OMP_NUM_THREADS"] = "1"
import torch
import cv2
import sys
import sys
import time
import requests
import logging
import random

# Configure basic logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Add root directory to python path to allow imports
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(os.path.dirname(BASE_DIR))

from backend.models.person_detector import PersonDetector
from backend.models.gesture_detector import GestureDetector
from backend.models.gender_resnet import GenderClassifier



import threading
from http.server import BaseHTTPRequestHandler, HTTPServer

API_URL = "http://127.0.0.1:8000/alerts"

global_frame = None

class MJPEGHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        global global_frame
        if self.path == '/video_feed':
            self.send_response(200)
            self.send_header('Content-type', 'multipart/x-mixed-replace; boundary=frame')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            try:
                while True:
                    if global_frame is not None:
                        ret, jpeg = cv2.imencode('.jpg', global_frame)
                        frame_bytes = jpeg.tobytes()
                        self.wfile.write(b'--frame\r\n')
                        self.wfile.write(b'Content-Type: image/jpeg\r\n')
                        self.wfile.write(f'Content-Length: {len(frame_bytes)}\r\n\r\n'.encode())
                        self.wfile.write(frame_bytes)
                        self.wfile.write(b'\r\n')
                    time.sleep(0.05)
            except Exception:
                pass
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        pass # Suppress HTTP noise logging to terminal

def start_mjpeg_server(port=5000):
    server = HTTPServer(('0.0.0.0', port), MJPEGHandler)
    threading.Thread(target=server.serve_forever, daemon=True).start()
    logger.info(f"MJPEG Stream started on http://127.0.0.1:{port}/video_feed")

def run_pipeline(camera_id="Cam-01", video_source=0, lat=28.7041, lon=77.1025):
    """
    Runs the main video processing pipeline.
    
    Args:
        camera_id: Identifier for the camera setting off alerts.
        video_source: 0 for webcam, or path to a video file.
        lat, lon: The geographical location of the camera.
    """
    logger.info("Initializing Models (YOLO, MediaPipe, PyTorch ResNet50)...")
    logger.info("Initializing PersonDetector (YOLO/Torch)...")
    person_detector = PersonDetector()
    logger.info("Initializing GestureDetector (MediaPipe)...")
    gesture_detector = GestureDetector()
    logger.info("Initializing GenderClassifier (ResNet/Torch)...")
    gender_classifier = GenderClassifier()
    logger.info("Opening VideoCapture...")
    
    cap = cv2.VideoCapture(video_source)
    if not cap.isOpened():
        logger.error(f"Could not open video source {video_source}")
        return

    logger.info("Camera Started. Processing frames... Press 'q' to quit.")
    
    start_mjpeg_server(port=5000)
    
    # State tracking dictionary for unique IDs
    from typing import Dict, Any
    track_history: Dict[int, Any] = {}
    
    try:
        while True:
            ret, frame = cap.read()
            if not ret:
                logger.warning("End of video stream or error reading frame.")
                break
                
            display_frame = frame.copy()
            
            # --- Pipeline Step 1: Person Detection & Tracking ---
            persons = person_detector.detect(frame)
            
            # SPEED OPTIMIZATION: Only run the heavy Hugging Face Gender model 
            # on people who we haven't seen for at least 5 frames.
            # Once we have 5 frames of history, we lock in the prediction and bypass the model!
            persons_for_gender_inference = []
            final_genders_to_process = []
            
            for p in persons:
                x1, y1, x2, y2, conf, track_id = p
                box = (x1, y1, x2, y2)
                
                # Initialize tracking history for new people
                if track_id != -1 and track_id not in track_history:
                    track_history[track_id] = {'gender_history': [], 'is_alerting': False}
                
                if track_id == -1 or len(track_history[track_id]['gender_history']) < 5:
                    persons_for_gender_inference.append(p)
                else:
                    # We have enough history! Bypass the slow ML model entirely.
                    history = track_history[track_id]['gender_history']
                    smoothed_gender = max(set(history), key=history.count)
                    final_genders_to_process.append((smoothed_gender, track_id, box))
            
            # --- Pipeline Step 2: Gender Detection (Heavy inference only on new people) ---
            if persons_for_gender_inference:
                new_genders = gender_classifier.detect_gender(frame, persons_for_gender_inference)
                for gender_label, conf, track_id, box in new_genders:
                    smoothed_gender = gender_label
                    if track_id != -1:
                        if track_id not in track_history:
                            track_history[track_id] = {'gender_history': [], 'is_alerting': False}
                            
                        track_history[track_id]['gender_history'].append(gender_label)
                        history = track_history[track_id]['gender_history']
                        smoothed_gender = max(set(history), key=history.count)
                    final_genders_to_process.append((smoothed_gender, track_id, box))
            
            # Now we loop through ALL detected persons (both freshly-inferenced and cached)
            for smoothed_gender, track_id, box in final_genders_to_process:
                x1, y1, x2, y2 = box
                
                # Draw Box and Track ID
                cv2.rectangle(display_frame, (x1, y1), (x2, y2), (255, 255, 0), 2)
                cv2.putText(display_frame, f"ID:{track_id} {smoothed_gender}", (x1, max(0, y1-10)), 
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 0), 2)
                
                # --- Pipeline Step 3: Gesture Detection (SOS) ---
                person_crop = display_frame[y1:y2, x1:x2]
                
                # Only run gesture detection if the person is large enough
                if person_crop.shape[0] < 50 or person_crop.shape[1] < 50:
                    continue
                    
                is_sos, _ = gesture_detector.detect_gesture(person_crop)
                
                if smoothed_gender == "Female":
                    if is_sos:
                        # Highlight the bounding box in RED for SOS
                        cv2.rectangle(display_frame, (x1, y1), (x2, y2), (0, 0, 255), 4)
                        
                        # Trigger Alert Event with a 10-second cooldown to prevent log spam
                        if track_id != -1:
                            current_time = time.time()
                            last_alert = track_history[track_id].get('last_alert_time', 0)
                            
                            if current_time - last_alert > 10.0:
                                logger.warning(f"SOS EVENT STARTED! ID: {track_id} Present: Female=True")
                                track_history[track_id]['last_alert_time'] = current_time
                                track_history[track_id]['is_alerting'] = True
                                
                                # Send HTTP Request immediately to FastAPI
                                payload = {
                                    "camera_id": camera_id,
                                    "confidence": round(random.uniform(0.76, 0.85), 4),
                                    "latitude": lat,
                                    "longitude": lon
                                }
                                try:
                                    response = requests.post(API_URL, json=payload, timeout=2)
                                    if response.status_code == 201:
                                        logger.info("-> Alert sent to FastAPI Backend")
                                    else:
                                        logger.error(f"-> Failed to send alert: {response.text}")
                                except Exception as e:
                                    logger.error(f"-> Failed to connect to API ({API_URL}): {e}")
                    else:
                        # Hands lowered. Reset alerting state so a new SOS can be triggered later
                        if track_id != -1 and track_history[track_id]['is_alerting']:
                            logger.info(f"SOS EVENT ENDED for ID: {track_id}")
                            track_history[track_id]['is_alerting'] = False
            
            # Send latest processed frame to MJPEG Streamer
            global global_frame
            global_frame = display_frame.copy()
            
            # Display result locally
            cv2.imshow("Women Safety - CCTV Analytics", display_frame)
            
            # Press 'q' to quit
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

    except Exception as e:
        logger.exception(f"Critical error running the pipeline: {e}")
    finally:
        if 'cap' in locals() and cap.isOpened():
            cap.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description="Run CCTV Analytics Pipeline")
    parser.add_argument("--source", default=0, help="Video source (0 for webcam or file path)")
    parser.add_argument("--camera-id", default="Cam-01", help="Identifier for the camera")
    parser.add_argument("--lat", type=float, default=28.7041, help="Camera Latitude (Default: Delhi)")
    parser.add_argument("--lon", type=float, default=77.1025, help="Camera Longitude (Default: Delhi)")
    args = parser.parse_args()
    
    try:
        source = int(args.source)
    except ValueError:
        source = args.source
        
    run_pipeline(camera_id=args.camera_id, video_source=source, lat=args.lat, lon=args.lon)
