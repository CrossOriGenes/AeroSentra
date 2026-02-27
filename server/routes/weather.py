from fastapi import APIRouter
from utils.helpers import RoverDevicePayload
from ml.predictor import predict
from services.weather_service import *

router = APIRouter()


# weather prediction according to rover payload
@router.post("/predict")
async def predict_weather(rover: RoverDevicePayload):
    try:

        lat = rover.lat
        lon = rover.lon
        
        raw = await fetch_weather(lat, lon)
        weather = extract_current_weather_data(raw)
        ml_features = build_ml_feature(rover, weather)
        result = predict(ml_features)
        
        response = { 
            "msg": "Prediction test successful.", 
            "data": { 
                "ml_data": result, 
                "api_data": { 
                    "coords": { 
                        "lat": lat, 
                        "lng": lon 
                    }, 
                    "city_name": weather["city"], 
                    "state": weather["state"], 
                    "country": weather["country"], 
                    "temp": rover.temperature, 
                    "humidity": rover.humidity, 
                    "uv_index": weather["uv"],
                    "pressure": weather["pressure"],
                    "wind": weather["wind"],
                    "aqi": extract_aqi(raw), 
                    "hourly_forecast": extract_hourly_forecast(raw),
                    "daily_forecast": extract_daily_forecast(raw),
                    "alerts": extract_alert(raw) 
                }
            }
        }
        
        return response
        
    except Exception as e:
        print(str(e))
        return { "msg": "Something went wrong!" }, 500
