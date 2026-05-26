<div align="center">

# 🛡️ Women Safety Analytics
### Protecting Women from Safety Threats — Real-Time AI Surveillance & Emergency Alert System

[![Python](https://img.shields.io/badge/Python-3.9+-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://python.org)
[![YOLOv8](https://img.shields.io/badge/YOLOv8-Ultralytics-FF6B35?style=for-the-badge)](https://docs.ultralytics.com)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com)
[![Streamlit](https://img.shields.io/badge/Streamlit-1.25+-FF4B4B?style=for-the-badge&logo=streamlit&logoColor=white)](https://streamlit.io)
[![MediaPipe](https://img.shields.io/badge/MediaPipe-Pose-0097A7?style=for-the-badge&logo=google&logoColor=white)](https://google.github.io/mediapipe)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<br/>

> **B.Tech Final Year Project** — Department of Computer Science & Engineering  
> Institute of Engineering and Technology, Lucknow  
> Dr. A.P.J. Abdul Kalam Technical University | June 2026

</div>

---

## 👥 Team

| Name | Roll No. |
|------|----------|
| Animesh Pratap Singh | 2200520100110 |
| Pratham Dixit | 2200520100135 |
| Varun Gupta | 2200520100163 |

**Supervisors:** Dr. Aditi Sharma &nbsp;|&nbsp; Er. Akash

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Problem Statement](#-problem-statement)
- [System Architecture](#-system-architecture)
- [Key Features](#-key-features)
- [AI/ML Pipeline](#-aiml-pipeline)
- [Dataset & Fine-Tuning](#-dataset--fine-tuning)
- [Performance Results](#-performance-results)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Installation](#-installation)
- [Running the System](#-running-the-system)
- [API Reference](#-api-reference)
- [Future Work](#-future-work)
- [References](#-references)

---

## 🔍 Overview

**Women Safety Analytics** is a real-time, AI-powered public safety system that transforms passive CCTV cameras into intelligent safety sentinels. Traditional safety mechanisms — helplines, panic buttons, or standard surveillance — are entirely victim-dependent and reactive. This system eliminates that dependency by automatically detecting distress signals and dispatching emergency alerts within **under 2 seconds**, without any manual intervention.

The system comprises two tightly integrated components:

**1. Automated AI Surveillance Pipeline**
A computer vision pipeline that processes live CCTV feeds using a fine-tuned YOLOv8 model, DeepSORT multi-person tracking, a Vision Transformer for gender classification, and MediaPipe Pose for SOS gesture recognition. When a threat scenario is detected (lone woman at night, SOS gesture), a location-tagged alert is automatically sent to a central dashboard.

**2. Mobile Safety Application**
A companion app (React Native) that empowers women with a one-tap SOS button, live GPS location sharing, and a crime hotspot safety map powered by DBSCAN clustering — enabling both reactive emergency response and proactive safe route planning.

---

## ❗ Problem Statement

Despite high CCTV penetration in Indian cities, monitoring infrastructure remains overwhelmingly **passive** — footage is reviewed only after incidents are reported. Existing safety tools share a critical flaw: they require the victim to consciously seek help, which is often impossible during a sudden attack or active stalking.

Key gaps addressed by this project:
- ⏱️ **Response delay** — manual CCTV monitoring takes 5–10 minutes vs. our **1.8-second** automated pipeline
- 🙋 **Victim dependency** — no system should require the victim to trigger the alert
- 📊 **No hotspot intelligence** — existing systems don't leverage historical data for proactive safety planning
- 🤖 **No AI integration** — current dashboards lack generative AI for automated incident reporting

---

## 🏗️ System Architecture

The system is composed of **five interconnected components**:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CCTV / Webcam Feed                       │
└──────────────────────────┬──────────────────────────────────────┘
                           │ OpenCV Frame Capture
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Computer Vision Pipeline                        │
│                                                                 │
│  YOLOv8 Detection → DeepSORT Tracking → Gender ViT → MediaPipe │
│       (Person Bounding Box)   (Track ID)  (Male/Female)  (Pose)│
│                                                                 │
│              SOS Gesture Detected? ──► POST /alerts             │
└─────────┬───────────────────────────────────────────────────────┘
          │                                    │
          │ MJPEG Stream (port 5000)           │ FastAPI REST
          ▼                                    ▼
┌──────────────────┐                 ┌──────────────────────┐
│ Operator          │                 │ FastAPI Backend       │
│ Dashboard         │                 │                      │
│ (Streamlit)       │◄────────────────│ SQLite Database      │
│                   │                 │ GPT-3.5-turbo AI     │
│ • Live Stats      │                 │ DBSCAN Hotspots      │
│ • Folium Map      │                 └──────────────────────┘
│ • Incident Log    │                          │
│ • AI Report Gen   │                          │ REST API
│ • Chat Assistant  │                 ┌──────────────────────┐
└──────────────────┘                 │ Mobile App            │
                                     │ (React Native)        │
                                     │                       │
                                     │ • Manual SOS Button   │
                                     │ • Live GPS Sharing    │
                                     │ • Hotspot Safety Map  │
                                     └──────────────────────┘
```

---

## ✨ Key Features

### 🎯 Automated Surveillance
- **Real-time person detection** at 28 FPS using fine-tuned YOLOv8 Nano
- **SOS gesture recognition** — detects both-hands-raised distress signal with 94% accuracy
- **Gender classification** using Vision Transformer with 15-frame majority-vote smoothing (91.2% accuracy)
- **Multi-person tracking** via DeepSORT with persistent track IDs across frames
- **Low-light enhancement** using CLAHE (Adaptive Histogram Equalization) for nighttime CCTV
- **Stalking detection** — flags same track ID repeatedly appearing near a female-classified track

### 🗺️ Smart Hotspot Analytics
- **DBSCAN clustering** on historical alert coordinates to identify high-risk zones
- **Folium HeatMap** visualization of crime density on interactive geospatial maps
- **Red zone marking** for safe route planning in the mobile app

### 🤖 Generative AI Integration
- **Automated incident reports** — GPT-3.5-turbo generates formal reports from alert metadata in 3–5 seconds
- **Natural language data queries** — ask the system questions about incidents in plain English (93.3% query accuracy)

### 📱 Mobile Application
- One-tap **manual SOS alert** with auto-location tagging
- **Live GPS location sharing** with authorities
- **Crime hotspot map** for proactive route safety planning
- **Authority broadcast** — push alerts to nearby users during active incidents

### 📊 Operator Dashboard
- Live metrics: total alerts, active cameras, recent incidents
- Embedded **MJPEG annotated video feed** (20–25 FPS in-browser)
- **Incident log table** with sortable, filterable records
- AI report generation and chat assistant built-in

---

## 🧠 AI/ML Pipeline

### Stage 1 — Person Detection (YOLOv8)
```python
# Fine-tuned YOLOv8 Nano inference
model = YOLO("best.pt")  # fine-tuned checkpoint
results = model(frame, conf=0.40, classes=[0])  # person class only
```
- Input resolution: 640×480
- Confidence threshold: 0.40 (high recall — misses cost more than false positives)
- Inference speed: **~35 ms/frame** on GTX 1660 Super

### Stage 2 — Multi-Person Tracking (DeepSORT)
```python
tracker = DeepSort(max_age=30, n_init=3)
tracks = tracker.update_tracks(detections, frame=frame)
# Each person gets a persistent track_id across frames
```
- Kalman filter for motion prediction
- Deep appearance descriptor for re-identification after occlusion
- Track IDs enable per-person gender smoothing and stalking detection

### Stage 3 — Gender Classification (Hugging Face ViT)
```python
# Throttled: runs every 15 frames per track_id
pipe = pipeline("image-classification", model="rizvandwiki/gender-classification")
# Majority-vote over 15 frames: single frame 84.6% → 15-frame 91.2%
```

### Stage 4 — Pose & SOS Gesture (MediaPipe)
```python
mp_pose = mp.solutions.pose
# 33 body landmarks extracted per person
# SOS heuristic: both wrists above shoulder landmarks
sos_detected = (left_wrist.y < left_shoulder.y) and (right_wrist.y < right_shoulder.y)
# Confirmed only if true in 3 of 5 consecutive frames (temporal voting)
```

### Stage 5 — Alert Dispatch
```python
# When SOS confirmed on female-classified track:
requests.post("http://localhost:8000/alerts", json={
    "camera_id": camera_id,
    "confidence": float(conf),
    "latitude": lat,
    "longitude": lng,
    "timestamp": datetime.utcnow().isoformat()
})
```

---

## 📊 Dataset & Fine-Tuning

### Datasets Used

| Source | Images | Purpose |
|--------|--------|---------|
| [SOS Gesture Dataset — Sri (2024)](https://universe.roboflow.com/sri-54yyj/sos-gesture) | ~3,200 | Hands-raised SOS distress gesture detection |
| [Women Safety Dataset — WomenSafety (2024)](https://universe.roboflow.com/womensafety/women-safety-trnib) | 410 | High-risk CCTV safety scenarios |
| **Combined (after de-duplication)** | **~3,580** | **Merged training dataset** |

### Dataset Split

| Split | Images | Percentage |
|-------|--------|------------|
| Training | 2,864 | 80% |
| Validation | 538 | 15% |
| Test | 178 | 5% |

### Data Augmentation

| Technique | Configuration |
|-----------|--------------|
| Random Horizontal Flip | 50% probability |
| Random Brightness Scaling | ±30% variation |
| Mosaic Augmentation | 4-image combination |
| Gaussian Noise | σ = 0.01–0.03 |
| HSV Colour Jitter | Hue ±0.015, Sat ±0.7, Val ±0.4 |
| Cutout / Random Erasing | 0.1–0.2 scale patches |
| Random Crop & Resize | 70–100% of image area |

### Fine-Tuning Configuration

| Hyperparameter | Value |
|----------------|-------|
| Base Model | YOLOv8 Nano (`yolov8n.pt`) — COCO pre-trained |
| Input Resolution | 640 × 640 px |
| Epochs | 50 |
| Batch Size | 16 |
| Learning Rate | 0.01 (cosine annealing decay) |
| Optimiser | SGD — momentum 0.937, weight decay 0.0005 |
| Warmup Epochs | 3 |
| Confidence Threshold | 0.40 |
| IoU Threshold (NMS) | 0.45 |
| Hardware | NVIDIA GTX 1660 Super (6 GB VRAM) |
| Training Time | ~2.5 hours |
| Best Checkpoint | Epoch 43 (minimum validation loss) |

---

## 📈 Performance Results

### YOLOv8 Detection

| Metric | Training | Validation (Real-World) |
|--------|----------|------------------------|
| Accuracy | 89.4% | 81.2% |
| Precision | 87.1% | 79.5% |
| Recall | 88.5% | 83.4% |
| F1-Score | 87.8% | 81.4% |
| **mAP@50** | **92.3%** | **84.6%** |

> 📌 Base COCO model mAP@50: ~61.4% → Fine-tuned: **84.6%** (+23.2 percentage points)

### Gender Classification

| Mode | Accuracy |
|------|----------|
| Single-frame (no smoothing) | 84.6% |
| 5-frame majority-vote | 88.3% |
| **15-frame majority-vote (production)** | **91.2%** |

### SOS Gesture Recognition

| Metric | Value |
|--------|-------|
| True Positive Rate | **94.0%** |
| False Positive Rate | 4.0% |
| FP Reduction (temporal voting) | 9.0% → 4.0% |

### End-to-End System Performance

| Metric | Result |
|--------|--------|
| YOLOv8 Inference Speed | 28 FPS @ 640×480 |
| YOLOv8 Detection latency | ~35 ms/frame |
| MediaPipe Pose latency | ~22 ms/frame |
| Gender classification latency | ~180 ms (throttled) |
| Alert POST to backend | ~45 ms |
| **End-to-End Alert Latency** | **1.8 s avg (±0.4 s)** |
| Dashboard Load (500 alerts) | < 2 s |
| MJPEG Stream Frame Rate | 20–25 FPS |
| AI Report Generation | 3–5 s/report |
| AI Assistant Query Accuracy | 93.3% (14/15 queries) |
| DBSCAN Hotspot Detection | 3/3 clusters correctly identified |

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| Video Capture & Annotation | OpenCV (`cv2`) |
| Person Detection | Ultralytics YOLOv8 Nano — fine-tuned |
| Training Datasets | Roboflow Universe (SOS Gesture + Women Safety) |
| Multi-Person Tracking | DeepSORT (Kalman filter + appearance descriptor) |
| Gender Classification | Hugging Face `rizvandwiki/gender-classification` (ViT) |
| Pose & Gesture Recognition | Google MediaPipe Pose (33 landmarks) |
| REST API Backend | FastAPI + Uvicorn (async Python) |
| Database | SQLite (`alerts_data.db`) |
| Generative AI | OpenAI GPT-3.5-turbo |
| Operator Dashboard | Streamlit + Pandas + Folium + streamlit-folium |
| Live Video Streaming | MJPEG HTTP server (port 5000) |
| Hotspot Analytics | DBSCAN (scikit-learn) + Folium HeatMap |
| Mobile Application | React Native |
| Language & Runtime | Python 3.9+ |
| GPU | NVIDIA GTX 1660 Super (CUDA 11.8) |

---

## 📁 Project Structure

```
women-safety-analytics/
│
├── backend/
│   ├── main.py                    # FastAPI app — all REST endpoints, CORS, AI features
│   ├── database/
│   │   └── db.py                  # SQLite helpers — insert_alert(), get_alerts()
│   └── scripts/
│       └── run_pipeline.py        # Main CV pipeline — YOLOv8 + DeepSORT + MediaPipe
│
├── models/
│   └── best.pt                    # Fine-tuned YOLOv8 Nano checkpoint (epoch 43)
│
├── streamlit_app.py               # Operator dashboard — stats, map, feed, AI tools
│
├── mobile/                        # React Native mobile application
│   ├── App.js
│   ├── screens/
│   │   ├── SOSScreen.js           # Manual SOS trigger + live GPS
│   │   └── MapScreen.js           # Crime hotspot safety map
│   └── api/
│       └── client.js              # FastAPI REST integration
│
├── training/
│   ├── fine_tune.py               # YOLOv8 fine-tuning script
│   ├── data.yaml                  # Dataset config (train/val/test paths + classes)
│   └── runs/                      # Training logs, loss curves, mAP plots
│
├── alerts_data.db                 # SQLite database (auto-created on first run)
├── requirements.txt               # Python dependencies
├── .env.example                   # Environment variables template
└── README.md
```

---

## ⚙️ Installation

### Prerequisites
- Python 3.9+
- NVIDIA GPU with CUDA 11.8+ (recommended: GTX 1660 Super or better)
- Node.js 18+ (for mobile app)

### 1. Clone the repository
```bash
git clone https://github.com/your-username/women-safety-analytics.git
cd women-safety-analytics
```

### 2. Create a virtual environment
```bash
python -m venv venv
source venv/bin/activate        # Linux/Mac
venv\Scripts\activate           # Windows
```

### 3. Install dependencies
```bash
pip install -r requirements.txt
```

<details>
<summary>📦 Key packages in requirements.txt</summary>

```
ultralytics>=8.0.145
mediapipe>=0.10.3
deep-sort-realtime
transformers>=4.30.0
fastapi>=0.100.0
uvicorn
openai>=1.0.0
streamlit>=1.25.0
folium
streamlit-folium
pandas
numpy
opencv-python
python-dotenv
scikit-learn
```
</details>

### 4. Set up environment variables
```bash
cp .env.example .env
```

Edit `.env`:
```env
OPENAI_API_KEY=your_openai_api_key_here
CAMERA_SOURCE=0                  # 0 for webcam, or path to video file
BACKEND_URL=http://localhost:8000
MJPEG_PORT=5000
```

### 5. Download the fine-tuned model
Place the fine-tuned checkpoint `best.pt` in the `models/` directory.  
*(Training instructions below if you want to fine-tune yourself.)*

---

## 🚀 Running the System

### Start all components (3 terminals):

**Terminal 1 — FastAPI Backend**
```bash
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```
> API docs available at: `http://localhost:8000/docs`

**Terminal 2 — Computer Vision Pipeline**
```bash
python backend/scripts/run_pipeline.py
```
> MJPEG stream available at: `http://localhost:5000`

**Terminal 3 — Streamlit Operator Dashboard**
```bash
streamlit run streamlit_app.py
```
> Dashboard available at: `http://localhost:8501`

---

### Fine-Tune the Model (Optional)

```bash
# Export datasets from Roboflow in YOLOv8 format, then:
python training/fine_tune.py

# Or directly with Ultralytics CLI:
yolo train model=yolov8n.pt data=training/data.yaml epochs=50 imgsz=640 batch=16
```

---

## 📡 API Reference

Base URL: `http://localhost:8000`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/alerts` | Create an alert — body: `{ camera_id, confidence, latitude, longitude, video_path? }` |
| `GET` | `/alerts` | Get recent alerts — query: `?limit=100` |
| `GET` | `/stats` | Aggregated stats: `total_alerts`, `active_cameras` |
| `DELETE` | `/alerts` | Clear all alerts (admin only) |
| `POST` | `/generate_report` | Generate AI incident report via GPT-3.5-turbo |
| `POST` | `/chat_query` | Natural-language query on alert data (RAG via GPT-3.5-turbo) |

**Example — Create Alert:**
```bash
curl -X POST http://localhost:8000/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "camera_id": "CAM_01",
    "confidence": 0.87,
    "latitude": 26.8467,
    "longitude": 80.9462
  }'
```

**Example — Get Stats:**
```bash
curl http://localhost:8000/stats
# Response: { "total_alerts": 42, "active_cameras": 3 }
```

---

## 🔭 Future Work

- **🌙 Low-Light & Thermal Imaging** — Nighttime-specific detector trained on infrared/thermal CCTV data
- **🤲 Extended Gesture Vocabulary** — LSTM/Transformer classifier for a broader distress gesture set beyond hands-raised
- **📡 Multi-Camera Federation** — Cross-camera track ID reconciliation for city-scale stalking detection
- **⚡ Edge Deployment** — Port CV pipeline to NVIDIA Jetson Nano or Coral TPU Raspberry Pi for standalone camera nodes
- **📱 Mobile App Production Release** — Offline-first hotspot caching, encrypted SOS messaging, battery-optimised background GPS
- **⚖️ Bias Auditing** — Formal demographic fairness audit of gender classifier and gesture recogniser before real-world deployment
- **🚨 DIAL 112 Integration** — Direct REST API bridge to India's national emergency system for zero-human-in-the-loop police dispatch
- **🔄 Continuous Learning Pipeline** — Active learning loop: sample uncertain detections → human review → incremental retraining

---

## 📚 References

1. S. Ren et al., "Faster R-CNN," IEEE TPAMI, 2017. [arxiv.org/pdf/1506.01497](https://arxiv.org/pdf/1506.01497)
2. C. Szegedy et al., "Inception-v4, Inception-ResNet," AAAI 2017. [arxiv.org/pdf/1602.07261](https://arxiv.org/pdf/1602.07261)
3. N. Wojke et al., "DeepSORT," IEEE ICIP 2017. [arxiv.org/pdf/1703.07402](https://arxiv.org/pdf/1703.07402)
4. Z. Cao et al., "OpenPose," IEEE TPAMI 2019. [arxiv.org/pdf/1812.08008](https://arxiv.org/pdf/1812.08008)
5. A. Bochkovskiy et al., "YOLOv4," arXiv 2020. [arxiv.org/pdf/2004.10934](https://arxiv.org/pdf/2004.10934)
6. R. Minhas et al., "Deep Learning-Based Anomaly Detection in Surveillance Videos," IEEE Access, 2023.
7. M. Ester et al., "DBSCAN," KDD'96. [cdn.aaai.org/KDD/1996/KDD96-037.pdf](https://cdn.aaai.org/KDD/1996/KDD96-037.pdf)
8. Ultralytics, "YOLOv8 Docs." [docs.ultralytics.com](https://docs.ultralytics.com)
9. Google, "MediaPipe Pose." [google.github.io/mediapipe/solutions/pose.html](https://google.github.io/mediapipe/solutions/pose.html)
10. Hugging Face, "rizvandwiki/gender-classification." [huggingface.co/rizvandwiki/gender-classification](https://huggingface.co/rizvandwiki/gender-classification)
11. Sri, "SOS Gesture Dataset," Roboflow Universe, 2024. [universe.roboflow.com/sri-54yyj/sos-gesture](https://universe.roboflow.com/sri-54yyj/sos-gesture)
12. WomenSafety, "Women Safety Dataset," Roboflow Universe, 2024. [universe.roboflow.com/womensafety/women-safety-trnib](https://universe.roboflow.com/womensafety/women-safety-trnib)

---

<div align="center">

**Made with ❤️ for women's safety**

*B.Tech Final Year Project — IET Lucknow, 2026*

</div>
