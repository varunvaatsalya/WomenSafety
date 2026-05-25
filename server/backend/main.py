from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
import sys
import os
from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

# Ensure we can import backend packages
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(BASE_DIR)

from backend.database.db import insert_alert, get_recent_alerts, clear_alerts

app = FastAPI(title="Women Safety API", description="Backend for CV Alert System")
client = OpenAI() # Uses OPENAI_API_KEY from .env

class AlertEvent(BaseModel):
    camera_id: str = "MOBILE_SOS"
    confidence: float = 1.0
    latitude: float
    longitude: float
    video_path: Optional[str] = None
    source: str = "mobile"  # 'cv' or 'mobile'

@app.post("/alerts", status_code=201)
def create_alert(alert: AlertEvent):
    """
    Registers a new SOS / safety alert. Can be from CV pipeline ('cv') or Mobile App ('mobile').
    """
    try:
        insert_alert(
            camera_id=alert.camera_id, 
            confidence=alert.confidence, 
            latitude=alert.latitude, 
            longitude=alert.longitude,
            video_path=alert.video_path,
            source=alert.source
        )
        return {"status": "success", "message": "Alert registered successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/alerts")
def fetch_alerts(limit: int = 100, hours_back: Optional[int] = None):
    """
    Retrieves alerts. Supports optional time filtering via hours_back.
    """
    alerts = get_recent_alerts(limit=limit, hours_back=hours_back)
    return {"status": "success", "data": alerts}

@app.get("/stats")
def fetch_stats():
    """
    Retrieves highly aggregated stats for the dashboard.
    """
    alerts = get_recent_alerts(limit=1000)
    cameras = set([a['camera_id'] for a in alerts])
    
    return {
        "status": "success",
        "data": {
            "total_alerts": len(alerts),
            "active_cameras": len(cameras)
        }
    }

@app.delete("/alerts")
def delete_all_alerts():
    """
    Clears all alerts from the database for a fresh session.
    """
    try:
        clear_alerts()
        return {"status": "success", "message": "All alerts cleared"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class ReportRequest(BaseModel):
    camera_id: str
    confidence: str
    timestamp: str
    source: str
    latitude: float
    longitude: float

@app.post("/generate_report")
def generate_report(req: ReportRequest):
    prompt = f"""
    You are an AI security dispatcher. An automated system has detected an incident.
    Write a brief, highly professional 1-paragraph incident report suitable for law enforcement dispatch.
    
    Incident Details:
    - Camera ID / Source: {req.camera_id} ({req.source})
    - Confidence Score: {req.confidence}
    - Timestamp: {req.timestamp}
    - Location (Lat, Lon): {req.latitude}, {req.longitude}
    
    Make it sound objective, formal, and urgent but calm. Do not add any conversational filler.
    """
    try:
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "You are a professional security dispatcher."},
                {"role": "user", "content": prompt}
            ]
        )
        report = response.choices[0].message.content
        return {"status": "success", "report": report}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class ChatRequest(BaseModel):
    query: str
    csv_data: str

@app.post("/chat_query")
def chat_query(req: ChatRequest):
    prompt = f"""
    You are an AI Security Assistant. Answer the user's question based strictly on the provided CSV data of recent SOS alerts.
    
    CSV Data:
    {req.csv_data}
    
    User Question: {req.query}
    
    Keep the answer concise and strictly relevant to the data provided. Do not hallucinate data.
    """
    try:
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "You are an AI data assistant."},
                {"role": "user", "content": prompt}
            ]
        )
        return {"status": "success", "answer": response.choices[0].message.content}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
