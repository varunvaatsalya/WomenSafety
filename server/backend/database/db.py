import os
import sqlite3
from datetime import datetime

# Define the DB path relative to this file
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DB_PATH = os.path.join(BASE_DIR, "alerts_data.db")

def init_db():
    """Initializes the database and creates necessary tables."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Added 'source' to distinguish between CCTV (cv) and Mobile (mobile) alerts
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS alerts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp TEXT NOT NULL,
            camera_id TEXT NOT NULL,
            confidence REAL NOT NULL,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL,
            video_path TEXT,
            source TEXT DEFAULT 'cv'
        )
    ''')
    
    # Ensure columns exist in case table was created previously without them
    try:
        cursor.execute('ALTER TABLE alerts ADD COLUMN video_path TEXT')
    except sqlite3.OperationalError:
        pass

    try:
        cursor.execute('ALTER TABLE alerts ADD COLUMN source TEXT DEFAULT "cv"')
    except sqlite3.OperationalError:
        pass
    
    conn.commit()
    conn.close()

def insert_alert(camera_id: str, confidence: float, latitude: float, longitude: float, video_path: str = None, source: str = "cv"):
    """Inserts a new alert into the database."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Use standard ISO format without milliseconds for easier SQLite date comparison if needed
    timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
    
    cursor.execute('''
        INSERT INTO alerts (timestamp, camera_id, confidence, latitude, longitude, video_path, source)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    ''', (timestamp, camera_id, confidence, latitude, longitude, video_path, source))
    
    conn.commit()
    conn.close()

def get_recent_alerts(limit=100, hours_back: int = None):
    """Fetches alerts, optionally filtered by timing."""
    try:
        conn = sqlite3.connect(DB_PATH)
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        
        if hours_back:
            # SQLite doesn't natively parse 'T' in datetime as easily as ' ', but it works with ISO8601
            # Using strftime to match our insert format
            cursor.execute('''
                SELECT id, timestamp, camera_id, confidence, latitude, longitude, video_path, source
                FROM alerts 
                WHERE timestamp >= datetime('now', ?)
                ORDER BY timestamp DESC 
                LIMIT ?
            ''', (f"-{hours_back} hours", limit))
        else:
            cursor.execute('''
                SELECT id, timestamp, camera_id, confidence, latitude, longitude, video_path, source
                FROM alerts 
                ORDER BY timestamp DESC 
                LIMIT ?
            ''', (limit,))
        
        rows = cursor.fetchall()
        conn.close()
        return [dict(row) for row in rows]
    except sqlite3.OperationalError:
        return []

def clear_alerts():
    """Clears all alerts from the database."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute('DELETE FROM alerts')
    conn.commit()
    conn.close()

# Auto-initialize DB when module is imported
init_db()
