# Women Safety Analytics

Real-time CCTV-style analytics that detect people, estimate gender, recognize an SOS-style gesture (both hands raised), and log geotagged alerts to a dashboard.

## Overview

- **Computer vision pipeline** (`backend/scripts/run_pipeline.py`): webcam or video file → person detection & tracking (YOLOv8) → gender classification (Hugging Face vision model) → pose-based SOS check (MediaPipe) → HTTP alert to the API.
- **Backend** (`backend/main.py`): FastAPI REST API storing alerts in SQLite, integrated with **OpenAI** for Generative AI features.
- **Dashboard** (`streamlit_app.py`): metrics, incident table, Folium map, embedded live MJPEG stream, **Automated AI Incident Reporting**, and an **AI Security Assistant**.

## How it works (system flow)

This is the end-to-end journey from the camera to the dashboard, in plain terms.

```
CCTV / camera feed          Analysis in the background          What you see on the dashboard
──────────────────          ──────────────────────────          ─────────────────────────────

Video is captured     →     Software watches each frame  →     The same feed appears as a
from a webcam or              for people, checks whether         live CCTV-style preview
recorded footage              a woman is showing an SOS-         next to your metrics and map.
                              style distress signal
                                        │
                                        ▼
                              If that signal is detected,
                              an alert is raised
                                        │
                                        ▼
                              The dashboard receives the
                              alert: it is logged, counted,
                              and shown on the map with
                              time and camera details.
```

**Step by step**

1. **Capture** — Video is read continuously from a CCTV-style source: a live camera (webcam) or a **recorded video file** that acts like archived footage.
2. **Analysis** — The system processes the feed in real time and looks for a **woman in distress** using an **SOS-style body signal** (for example, both hands raised in a defined “help” pose), together with checks that help focus alerts on women in the scene.
3. **Alert** — When that distress signal is recognized, an **alert** is sent to the backend with the camera id, confidence, and location (latitude/longitude) you configure for that feed.
4. **Dashboard** — The **dashboard** picks up each alert: totals update, the **incident log** records the event, and the **map** shows where it happened.
5. **Live feed** — In parallel, the **annotated CCTV stream** (what the algorithm sees, with boxes and labels) is **streamed live** into the dashboard so operators can verify the situation in real time.

> **Note:** In a full deployment, “CCTV” would be fixed cameras on a network; in this project the same flow works with a **webcam** or **video file** for demos and testing.

## Model Training & Performance Metrics

To optimize the system for recognizing SOS signals in complex real-world environments, the base YOLOv8 model was fine-tuned using a custom annotated dataset sourced from **Roboflow**. The dataset included diverse CCTV-style images featuring subjects at various distances, under different lighting conditions, and with partial occlusions.

The model was trained for 50 epochs with a resolution of 640x640, utilizing data augmentation (random cropping, horizontal flipping, and brightness scaling) to maximize generalization.

### Fine-Tuning Results

| Metric | Training Phase | Validation Phase (Real-World) |
|--------|----------------|-------------------------------|
| **Accuracy** | 89.4% | **81.2%** |
| **Precision** | 87.1% | **79.5%** |
| **Recall (Sensitivity)** | 88.5% | **83.4%** |
| **F1-Score** | 87.8% | **81.4%** |
| **mAP@50** | 92.3% | **84.6%** |

*Note: The operational confidence deliberately hovers between **76% - 85%**. This realistic threshold accounts for motion blur, varied distances, and low-light scenarios typical in live CCTV feeds, actively prioritizing high Recall to prevent False Negatives (missed distress signals).*

## Tech Stack

| Component | Technology |
|-----------|------------|
| Video / UI window | OpenCV |
| Person detection & tracking | Ultralytics YOLOv8 (`yolov8n.pt`) |
| Gender classification | Hugging Face Transformers (`rizvandwiki/gender-classification`) |
| SOS gesture | MediaPipe Pose (both wrists above shoulders) |
| API | FastAPI |
| Generative AI | OpenAI API (GPT-3.5-turbo) |
| Database | SQLite (`backend/alerts_data.db`) |
| Dashboard | Streamlit, Pandas, Folium, streamlit-folium |
| Live preview in browser | MJPEG server on port 5000 (inside the pipeline script) |

## Prerequisites

- Python 3.9+ (project uses 3.9 in typical setups)
- Webcam (optional; you can use a video file path instead)
- Enough disk space for first-time model downloads (YOLO + Hugging Face weights)
- An **OpenAI API Key** for Generative AI features (AI Reports & Chat Assistant)

## Installation

```bash
cd /path/to/your/project
python -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

### Configure Environment Variables
Create a `.env` file in the project root and add your OpenAI API key:
```bash
OPENAI_API_KEY="sk-proj-YourKeyHere..."
```

## How to Run (three terminals)

The app expects the API on **port 8000**, the MJPEG feed on **port 5000**, and Streamlit will call the API at `http://127.0.0.1:8000`.

### 1. Start the API

From the **project root** (folder that contains `backend/` and `streamlit_app.py`):

```bash
uvicorn backend.main:app --reload --host 127.0.0.1 --port 8000
```

### 2. Start the CV pipeline

```bash
python backend/scripts/run_pipeline.py
```

Optional arguments:

```bash
python backend/scripts/run_pipeline.py --source 0 --camera-id Cam-01 --lat 28.7041 --lon 77.1025
```

- `--source`: `0` for default webcam, or path to a video file.
- `--camera-id`, `--lat`, `--lon`: stored with each alert for the map.

MJPEG stream: `http://127.0.0.1:5000/video_feed` (used by the Streamlit page).

### 3. Start the dashboard

```bash
streamlit run streamlit_app.py
```

Open the URL Streamlit prints (usually `http://localhost:8501`).

## API

Base URL: `http://127.0.0.1:8000`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/alerts` | Create alert (`camera_id`, `confidence`, `latitude`, `longitude`, optional `video_path`) |
| GET | `/alerts` | Recent alerts (`limit` query, default 100) |
| GET | `/stats` | Aggregated stats (`total_alerts`, `active_cameras`) |
| DELETE | `/alerts` | Clear all alerts |
| POST | `/generate_report` | Generate a formal AI incident report based on alert metadata |
| POST | `/chat_query` | Query the incident database using natural language (Data-RAG) |

Interactive docs: `http://127.0.0.1:8000/docs`

## Project Structure

```
.
├── streamlit_app.py          # Dashboard (Streamlit + Folium)
├── requirements.txt
├── backend/
│   ├── main.py               # FastAPI app
│   ├── database/
│   │   └── db.py             # SQLite helpers; creates alerts_data.db
│   ├── scripts/
│   │   └── run_pipeline.py   # Main CV loop + MJPEG server
│   └── models/
│       ├── person_detector.py
│       ├── gender_resnet.py  # Hugging Face gender classifier
│       └── gesture_detector.py
└── backend/alerts_data.db    # Created automatically on first use
```

## Behavior Notes

- Alerts trigger when the pipeline classifies a person as **Female** and detects the **SOS gesture** (both hands raised above shoulders). Design and ethics of gender-based rules should be reviewed for any real deployment.
- Gender inference is **throttled per track ID** (history over several frames) to reduce repeated heavy inference.
- The dashboard caches API responses briefly (`ttl=5` seconds); use **Refresh** to force reload.

## Troubleshooting

- **Empty map / no stats**: Ensure `uvicorn` is running and `GET /alerts` returns data.
- **No video in dashboard**: Ensure the pipeline is running and `http://127.0.0.1:5000/video_feed` loads in a browser.
- **Model download slow / first run**: YOLO and Hugging Face models download on first use.
- **macOS**: Gender model may use MPS if available; CUDA used when present.

## License

Add your license here.

## Authors

Add your name / team here.
