from ultralytics import YOLO

class PersonDetector:
    def __init__(self, model_name='yolov8n.pt'):
        """
        Initializes the YOLO model for person detection.
        Uses yolov8n.pt by default (nano model for speed).
        """
        # Load the model
        # YOLOv8 will automatically download the model file if it doesn't exist
        self.model = YOLO(model_name)
    
    def detect(self, frame):
        """
        Detects persons in a given frame.
        
        Args:
            frame: A numpy array representing the image/frame using BGR format (OpenCV default).
            
        Returns:
            A list of bounding boxes for detected persons.
            Format: [(x1, y1, x2, y2, confidence), ...]
        """
        # Run tracking on the frame (Persist=True keeps object IDs across frames)
        # We set classes=0 to only detect 'person' (COCO class 0)
        results = self.model.track(frame, classes=0, verbose=False, persist=True)
        
        persons = []
        for result in results:
            boxes = result.boxes
            for box in boxes:
                # Get coordinates
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                confidence = box.conf[0].item()
                
                # Get tracking ID if available
                track_id = int(box.id[0].item()) if box.id is not None else -1
                
                persons.append((int(x1), int(y1), int(x2), int(y2), confidence, track_id))
                
        return persons
