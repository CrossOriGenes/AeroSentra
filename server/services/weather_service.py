import httpx, asyncio, os
from datetime import datetime
from services.image_service import get_place_images
from dotenv import load_dotenv

load_dotenv()

API_KEY = os.getenv("WEATHERAPI_KEY")
API_KEY_OPENWEATHERMAP = os.getenv("OPENWEATHERMAP_API_KEY")


# main forecast weather fetcher
async def fetch_weather(lat: float, lon: float):
    url = f"http://api.weatherapi.com/v1/forecast.json?key={API_KEY}&q={lat},{lon}&days=7&aqi=yes&alerts=yes"
    async with httpx.AsyncClient() as client:
        res = await client.get(url)
    return res.json()

# main current weather fetcher
async def fetcher_weather_v2(lat: float, lon: float):
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API_KEY_OPENWEATHERMAP}&units=metric"
    async with httpx.AsyncClient() as client:
        res = await client.get(url)
    return res.json()

# raw weather formatter
def extract_current_weather_data(data1, data2):
    current = data1["current"]
    location = data1["location"]
    
    return {
        "city": data2["name"],
        "state": location["region"],
        "country": location["country"],
        "temp": current["temp_c"],
        "is_day": True if current["is_day"] == 1 else False, 
        "max_temp": data1["forecast"]["forecastday"][0]["day"]["maxtemp_c"],
        "min_temp": data1["forecast"]["forecastday"][0]["day"]["mintemp_c"],
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
def build_ml_feature(device, weather):
    now = datetime.now()
    
    return {
        "temperature": device.temperature,
        "humidity": device.humidity,
        "pressure": weather["pressure"],
        "wind_speed": weather["wind"],
        "hour": now.hour,
        "day": now.day,
        "month": now.month
    }
    
def build_ml_features_v2(weather):
    now = datetime.now()
    
    return {
        "temperature": weather["temp"],
        "humidity": weather["humidity"],
        "pressure": weather["pressure"],
        "wind_speed": weather["wind"],
        "hour": now.hour,
        "day": now.day,
        "month": now.month
    }
    

# get nearby places for map
USERNAME = os.getenv("GEONAME_USER")

async def fetch_nearby_places(lat: float, lon: float):
    url = f"http://api.geonames.org/findNearbyPlaceNameJSON?lat={lat}&lng={lon}&radius=20&maxRows=5&username={USERNAME}"
    async with httpx.AsyncClient() as client:
        res = await client.get(url)
    return res.json()

async def fetch_nearby_places_current_weather(lat: float, lon: float):
    url = f"http://api.weatherapi.com/v1/current.json?key={API_KEY}&q={lat},{lon}"
    async with httpx.AsyncClient() as client:
        res = await client.get(url)
    return res.json()
    
    
def extract_places(data):
    places = data["geonames"]
    
    return [
        {
            "city": place["name"],
            "lat": place["lat"],
            "lng": place["lng"]
        }
        for place in places
    ]
    
async def attach_weather(places):
    
    async def process_place(place):
        data = await fetch_nearby_places_current_weather(place["lat"], place["lng"])
        current = data["current"]
        location = data["location"]
        
        return {
            "city": place["city"],
            "region": f"{location["region"]}, {location["country"]}",
            "lat": place["lat"],
            "lng": place["lng"],
            "temperature": current["temp_c"],
            "pressure": current["pressure_mb"],
            "humidity": current["humidity"],
            "uv": current["uv"],
            "icon": current["condition"]["icon"],
            "type": current["condition"]["text"],
        }
    
    tasks = [process_place(place) for place in places]
    
    return await asyncio.gather(*tasks)
