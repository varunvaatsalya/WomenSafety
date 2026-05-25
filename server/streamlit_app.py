import streamlit as st
import pandas as pd
import folium
from streamlit_folium import st_folium
import requests

API_URL = "http://127.0.0.1:8000"

# --- Page Configuration ---
st.set_page_config(
    page_title="Women Safety Analytics",
    page_icon="🛡️",
    layout="wide",
)

# --- CSS Styling ---
st.markdown("""
<style>
    .metric-card {
        background-color: white;
        padding: 20px;
        border-radius: 10px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        text-align: center;
        border-left: 5px solid #ff4b4b;
    }
    .metric-val {
        font-size: 2rem;
        font-weight: bold;
        color: #ff4b4b;
    }
    .metric-label {
        font-size: 1rem;
        color: #666;
    }
</style>
""", unsafe_allow_html=True)

# --- Sidebar Configuration ---
st.sidebar.title("🛠️ Control Panel")
timeframe_option = st.sidebar.selectbox(
    "Set Heatmap Timeframe",
    options=["Last 5 Hours", "Last 24 Hours", "Last 2 Days", "All Time"],
    index=3
)

# Convert timeframe to hours_back
timeframe_map = {
    "Last 5 Hours": 5,
    "Last 24 Hours": 24,
    "Last 2 Days": 48,
    "All Time": None
}
hours_back = timeframe_map[timeframe_option]

# --- Fetch Data ---
@st.cache_data(ttl=5)
def fetch_dashboard_data(h_back):
    try:
        params = {"hours_back": h_back} if h_back else {}
        alerts_res = requests.get(f"{API_URL}/alerts", params=params, timeout=2)
        stats_res = requests.get(f"{API_URL}/stats", timeout=2)
        
        alerts = alerts_res.json().get("data", []) if alerts_res.status_code == 200 else []
        stats = stats_res.json().get("data", {"total_alerts": 0, "active_cameras": 0}) if stats_res.status_code == 200 else {"total_alerts": 0, "active_cameras": 0}
        
        return alerts, stats
    except Exception as e:
        return [], {"total_alerts": 0, "active_cameras": 0}

alerts_data, stats_data = fetch_dashboard_data(hours_back)
df = pd.DataFrame(alerts_data)

# --- Dashboard Header ---
st.title("🛡️ Women Safety Alert Analytics")
st.markdown("Geospatial analytics dashboard aggregating SOS signals detected by local CCTV networks.")

st.write("---")

cols = st.columns([1, 1, 1, 1, 1])
if cols[2].button("🔄 Refresh Data", use_container_width=True):
    fetch_dashboard_data.clear()
    st.rerun()

if not df.empty:
    csv_data = df.to_csv(index=False).encode('utf-8')
    cols[3].download_button(
        label="⬇️ Download CSV",
        data=csv_data,
        file_name="sos_alerts_export.csv",
        mime="text/csv",
        use_container_width=True
    )
    
    if cols[4].button("🗑️ Clear All Alerts", use_container_width=True, type="primary"):
        try:
            resp = requests.delete(f"{API_URL}/alerts", timeout=2)
            if resp.status_code == 200:
                fetch_dashboard_data.clear()
                st.success("✅ All alerts cleared! The dashboard is now fresh.")
                st.rerun()
            else:
                st.error("Failed to clear alerts. Is the backend running?")
        except Exception:
            st.error("Could not connect to the backend.")

# --- SOS Emergency Banner ---
if not df.empty:
    latest_alert = df.iloc[0]
    latest_alert_time = latest_alert['timestamp']
else:
    latest_alert_time = "N/A"

m1, m2, m3 = st.columns(3)
with m1:
    st.markdown(f'<div class="metric-card"><div class="metric-val">{stats_data["total_alerts"]}</div><div class="metric-label">Total SOS Alerts Triggered</div></div>', unsafe_allow_html=True)
with m2:
    st.markdown(f'<div class="metric-card"><div class="metric-val">{stats_data["active_cameras"]}</div><div class="metric-label">Active Cameras Monitoring</div></div>', unsafe_allow_html=True)
with m3:
    st.markdown(f'<div class="metric-card" style="border-left-color: #2196f3;"><div class="metric-val" style="color: #2196f3; font-size: 1.5rem;">{latest_alert_time}</div><div class="metric-label">Latest Incident Timestamp</div></div>', unsafe_allow_html=True)

if not df.empty:
    if st.button("📝 Generate Formal Incident Report for Latest Alert", use_container_width=True):
        with st.spinner("Generating formal report via AI..."):
            latest_alert = df.iloc[0]
            payload = {
                "camera_id": str(latest_alert['camera_id']),
                "confidence": f"{latest_alert['confidence']*100:.2f}%",
                "timestamp": str(latest_alert['timestamp']),
                "source": str(latest_alert['source']),
                "latitude": float(latest_alert['latitude']),
                "longitude": float(latest_alert['longitude'])
            }
            try:
                res = requests.post(f"{API_URL}/generate_report", json=payload, timeout=10)
                if res.status_code == 200:
                    st.info(res.json().get("report"))
                else:
                    st.error("Failed to generate report. Make sure OPENAI_API_KEY is set in backend .env")
            except Exception as e:
                st.error(f"Error connecting to backend for report: {e}")

st.write("---")

# --- Main Content Area: Map and Log ---
if df.empty:
    st.info("No alerts recorded yet. The system is monitoring securely, or the backend is offline.")
else:
    col1, col2, col3 = st.columns([1.2, 1.2, 1])
    
    with col1:
        st.subheader("📍 Geospatial Map")
        st.markdown("Live plot of where SOS alerts were triggered.")
        
        # Center the map at the average location, or defaulting to something safe if list empty
        avg_lat = df['latitude'].mean()
        avg_lon = df['longitude'].mean()
        
        m = folium.Map(location=[avg_lat, avg_lon], zoom_start=13, tiles="CartoDB positron")
        
        for _, row in df.iterrows():
            is_mobile = row.get('source') == 'mobile'
            color = "orange" if is_mobile else "red"
            icon = "mobile" if is_mobile else "info-sign"
            
            folium.Marker(
                [row['latitude'], row['longitude']],
                popup=f"Source: {row.get('source', 'cv').upper()}<br>ID: {row['camera_id']}<br>Time: {row['timestamp']}<br>Confidence: {row['confidence']*100:.1f}%",
                tooltip="SOS Alert" if is_mobile else "CV Alert",
                icon=folium.Icon(color=color, icon=icon, prefix='fa' if is_mobile else 'glyphicon')
            ).add_to(m)
            
            # Draw a circle around it for area of impact
            folium.Circle(
                radius=100,
                location=[row['latitude'], row['longitude']],
                color="crimson" if not is_mobile else "orange",
                fill=True,
                fill_opacity=0.4 if is_mobile else 0.7
            ).add_to(m)

        # Call to render Folium map in Streamlit
        st_folium(m, width=400, height=450, key="heatmap_map")
        
    with col2:
        st.subheader("📹 Live CCTV Feed")
        st.markdown("Real-time AI camera tracking stream.")
        
        # We embed the MJPEG stream from run_pipeline.py directly!
        st.markdown(
            '<div style="text-align: center;"><img src="http://127.0.0.1:5000/video_feed" width="100%" '
            'style="border-radius: 10px; border: 2px solid #ccc; max-width: 500px;"></div>', 
            unsafe_allow_html=True
        )
        
    with col3:
        st.subheader("📋 Incident Log")
        display_df = df[['source', 'camera_id', 'timestamp', 'confidence']].copy()
        display_df['confidence'] = display_df['confidence'].apply(lambda x: f"{x*100:.2f}%")
        display_df['source'] = display_df['source'].str.upper()
        
        st.dataframe(display_df, hide_index=True, use_container_width=True, height=450)

# --- AI Chat ---
st.write("---")
st.subheader("🤖 AI Security Assistant")
st.markdown("Ask natural language questions about the incident logs. The AI will analyze the data and answer.")
user_query = st.text_input("Example: 'Which camera had the most alerts?' or 'What is the average confidence score?'")

if st.button("Ask AI", type="primary"):
    if df.empty:
        st.warning("No data available to analyze.")
    elif not user_query:
        st.warning("Please enter a question.")
    else:
        with st.spinner("AI is analyzing the database..."):
            try:
                csv_data = df.to_csv(index=False)
                res = requests.post(f"{API_URL}/chat_query", json={"query": user_query, "csv_data": csv_data}, timeout=20)
                if res.status_code == 200:
                    st.success(res.json().get("answer"))
                else:
                    st.error("Failed to get answer from AI. Make sure OPENAI_API_KEY is set in backend .env")
            except Exception as e:
                st.error(f"Error connecting to backend: {e}")

# --- Footer ---
st.markdown(
    """
    <div style='text-align: center; margin-top: 50px; color: #888;'>
        <p>Built with FastAPI, PyTorch (ResNet50), Streamlit, Folium, and OpenCV. GenAI powered by OpenAI.</p>
    </div>
    """,
    unsafe_allow_html=True
)
