import logging
from PIL import Image
import torch
from transformers import pipeline
import cv2

logger = logging.getLogger(__name__)

class GenderClassifier:
    def __init__(self):
        """
        Initializes a Hugging Face Transformers pipeline for high-accuracy gender classification.
        Switched to a state-of-the-art Vision model to fix inaccurate detections from previous models.
        Automatically caches the Hugging Face weights locally.
        """
        logger.info("Loading high-accuracy Hugging Face gender model (~300MB, downloads once)...")
        
        # Use Apple Silicon (MPS) or GPU if available for faster inference
        if torch.cuda.is_available():
            self.device = 0
        elif torch.backends.mps.is_available():
            self.device = "mps"
        else:
            self.device = -1
            
        # State of the art gender classification vision model
        self.classifier = pipeline("image-classification", model="rizvandwiki/gender-classification", device=self.device)

    def detect_gender(self, frame, persons):
        """
        Detects gender for the given list of persons in the frame.
        """
        genders_found = []
        
        # Convert frame for PIL 
        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        pil_image = Image.fromarray(frame_rgb)
        
        for person in persons:
            x1, y1, x2, y2, conf, track_id = person
            box = (x1, y1, x2, y2)
            
            # Basic bound safety
            x1, y1 = max(0, x1), max(0, y1)
            x2 = min(pil_image.width, x2)
            y2 = min(pil_image.height, y2)
            
            # Crop the person/face
            crop = pil_image.crop((x1, y1, x2, y2))
            
            # If crop is too small, skip
            if crop.width < 10 or crop.height < 10:
                continue
                
            try:
                # Perform inference
                results = self.classifier(crop)
                
                # results is typically [{'label': 'female', 'score': 0.99}, ...]
                best_match = results[0]
                label_raw = best_match['label'].lower()
                
                # Standardize outputs
                if 'female' in label_raw or 'woman' in label_raw:
                    gender_label = 'Female'
                else:
                    gender_label = 'Male'
                    
                confidence = best_match['score']
                
                # Ensure it fits the expected return format
                genders_found.append((gender_label, float(confidence), track_id, box))
            except Exception as e:
                logger.error(f"Gender classification inference error: {e}")
            
        return genders_found
