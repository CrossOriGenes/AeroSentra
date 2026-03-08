import httpx, os
from datetime import datetime

API_KEY = os.getenv("WEATHERAPI_KEY")


# main weather fetcher
async def fetch_weather(lat: float, lon: float):
    url = f"http://api.weatherapi.com/v1/forecast.json?key={API_KEY}&q={lat},{lon}&days=7&aqi=yes&alerts=yes"
    async with httpx.AsyncClient() as client:
        res = await client.get(url)
    return res.json()

# raw weather formatter
def extract_current_weather_data(data):
    current = data["current"]
    location = data["location"]
    
    return {
        "city": location["name"],
        "state": location["region"],
        "country": location["country"],
        "temp": current["temp_c"],
        "max_temp": data["forecast"]["forecastday"][0]["day"]["maxtemp_c"],
        "min_temp": data["forecast"]["forecastday"][0]["day"]["mintemp_c"],
        "feels_like": current["feelslike_c"],
        "humidity": current["humidity"],
        "pressure": current["pressure_mb"],
        "wind": current["wind_kph"],
        "wind_dir": current["wind_dir"],
        "uv": current["uv"],
        "precipitation": current["precip_mm"],
        "condition": current.get("condition", {}),
    }

# hourly forecast array for 24 hrs
def extract_hourly_forecast(data):
    hours = data["forecast"]["forecastday"][0]["hour"]
    
    return [
        {
            "time": h["time"],
            "temp": h["temp_c"],
            "icon": h["condition"]["icon"],
            "chance_of_rain": h["chance_of_rain"],
            "is_current": h["time"].endswith(str(datetime.now().hour).zfill(2) + ":00")
        }
        for h in hours
    ]

# daily forecast array for 7 days
def extract_daily_forecast(data):
    days = data["forecast"]["forecastday"]
    
    return [
        {
            "date": d["date"],
            "max_temp": d["day"]["maxtemp_c"],
            "min_temp": d["day"]["mintemp_c"],
            "avg_temp": d["day"]["avgtemp_c"],
            "icon": d["day"]["condition"]["icon"],
            "chance_of_rain": d["day"]["daily_chance_of_rain"],
            "uv": d["day"]["uv"]
        }
        for d in days
    ]

# AQI extractor
def extract_aqi(data):
    air = data["current"].get("air_quality", {})
    
    return {
        "pm2_5": air.get("pm2_5"),
        "pm10": air.get("pm10"),
        "co": air.get("co"),
        "no2": air.get("no2"),
        "o3": air.get("o3"),
        "so2": air.get("so2"),
        "magnitude_of_5": air.get("us-epa-index"),
        "magnitude_of_10": air.get("gb-defra-index"), 
    }
    
# weather alert extractor
def extract_alert(data):
    alerts = data.get("alerts", {}).get("alert", {})
    
    return [
        {
            "headline": a["headline"],
            "severity": a["severity"],
            "areas": a["areas"],
            "desc": a["desc"] 
        }
        for a in alerts
    ]

# ml feature builder
def build_ml_feature(rover, weather):
    now = datetime.now()
    
    return {
        "temperature": rover.temperature,
        "humidity": rover.humidity,
        "pressure": weather["pressure"],
        "wind_speed": weather["wind"],
        "hour": now.hour,
        "day": now.day,
        "month": now.month
    }