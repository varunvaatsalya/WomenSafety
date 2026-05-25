import cv2
import mediapipe as mp

class GestureDetector:
    def __init__(self):
        """
        Initializes MediaPipe for pose detection.
        """
        self.mp_pose = mp.solutions.pose
        self.pose = self.mp_pose.Pose(
            static_image_mode=False,
            model_complexity=1, # 0, 1 or 2 (1 is default balance of speed vs accuracy)
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5
        )
        self.mp_drawing = mp.solutions.drawing_utils

    def check_sos_gesture(self, landmarks):
        """
        Checks if the detected pose landmarks match an SOS gesture 
        (e.g., both hands raised above shoulders).
        """
        if not landmarks:
            return False
            
        # Get coordinates for left and right shoulders
        left_shoulder = landmarks.landmark[self.mp_pose.PoseLandmark.LEFT_SHOULDER]
        right_shoulder = landmarks.landmark[self.mp_pose.PoseLandmark.RIGHT_SHOULDER]
        
        # Get coordinates for wrists
        left_wrist = landmarks.landmark[self.mp_pose.PoseLandmark.LEFT_WRIST]
        right_wrist = landmarks.landmark[self.mp_pose.PoseLandmark.RIGHT_WRIST]
        
        # In MediaPipe, Y-axis goes from top (0.0) to bottom (1.0).
        # So, if wrist 'y' is LESS than shoulder 'y', the wrist is ABOVE the shoulder.
        left_hand_raised = left_wrist.y < left_shoulder.y
        right_hand_raised = right_wrist.y < right_shoulder.y
        
        # Check if BOTH hands are visibly raised above shoulders
        # Also ensure visibility confidence is decent
        visibility_threshold = 0.5
        both_visible = (left_wrist.visibility > visibility_threshold and 
                        right_wrist.visibility > visibility_threshold and
                        left_shoulder.visibility > visibility_threshold and
                        right_shoulder.visibility > visibility_threshold)
                        
        if both_visible and left_hand_raised and right_hand_raised:
            return True
            
        return False

    def detect_gesture(self, frame):
        """
        Detects gestures in a given video frame.
        
        Args:
            frame: A BGR image frame (OpenCV default).
            
        Returns:
            is_sos: Boolean indicating if an SOS gesture was detected.
            annotated_frame: Frame with pose landmarks drawn.
        """
        # Convert the BGR image to RGB as Mediapipe requires RGB
        image_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        
        # Process the image and detect poses
        results = self.pose.process(image_rgb)
        
        is_sos = False
        
        if results.pose_landmarks:
            # Draw landmarks on the frame for visualization
            self.mp_drawing.draw_landmarks(
                frame, 
                results.pose_landmarks, 
                self.mp_pose.POSE_CONNECTIONS
            )
            
            # Check for specific SOS gesture
            is_sos = self.check_sos_gesture(results.pose_landmarks)
            
        return is_sos, frame
