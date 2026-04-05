from fastapi import APIRouter
from schema.reqresmodels import RoverDevicePayload, UiDevicePayload
from ml.predictor import predict
from services.weather_service import *
from services.image_service import attach_images

router = APIRouter()


# weather prediction according to rover payload
@router.post("/predict")
async def predict_weather(rover: RoverDevicePayload):
    try:

        lat = rover.lat
        lon = rover.lon
        
        raw = await fetch_weather(lat, lon)
        raw2 = await fetcher_weather_v2(lat, lon)
        weather = extract_current_weather_data(raw, raw2)
        ml_features = build_ml_feature(rover, weather)
        result = predict(ml_features)
        
        response = { 
            "success": True,
            "msg": "Region weather predicted successfully.", 
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
                    "max_temp": weather["max_temp"], 
                    "min_temp": weather["min_temp"],
                    "feels_like": weather["feels_like"],
                    "humidity": rover.humidity, 
                    "uv_index": weather["uv"],
                    "pressure": weather["pressure"],
                    "wind": weather["wind"],
                    "wind_dir": weather["wind_dir"],
                    "precipitation": weather["precipitation"],
                    "condition": weather.get("condition"),
                    "is_day": weather["is_day"],
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
    

# weather prediction according to android payload
@router.post("/predict/v2")
async def predict_weather_v2(device: UiDevicePayload):
    try:
        lat = device.lat
        lon = device.lon
        
        raw = await fetch_weather(lat, lon)
        raw2 = await fetcher_weather_v2(lat, lon)
        weather = extract_current_weather_data(raw, raw2)
        ml_features = build_ml_features_v2(weather)
        result = predict(ml_features)
        
        response = { 
            "success": True,
            "msg": "Region weather predicted successfully.", 
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
                    "temp": weather["temp"], 
                    "max_temp": weather["max_temp"], 
                    "min_temp": weather["min_temp"],
                    "feels_like": weather["feels_like"],
                    "humidity": weather["humidity"], 
                    "uv_index": weather["uv"],
                    "pressure": weather["pressure"],
                    "wind": weather["wind"],
                    "wind_dir": weather["wind_dir"],
                    "precipitation": weather["precipitation"],
                    "visibility": weather["visibility"],
                    "condition": weather.get("condition"),
                    "is_day": weather["is_day"],
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


# get nearby places in map
@router.get("/map_nearby_places")
async def get_nearby_map_places(lat: float, lng: float):
    try:
        raw = await fetch_nearby_places(lat, lng)
        places = extract_places(raw)
        places = await attach_weather(places)
        result = await attach_images(places)
        
        return {
            "success": True,
            "places": result,
            "message": f"Nearby Places for -> Latitude: {lat}, Longitude: {lng}"
        }
    except Exception as e:
        print(str(e))
        return { "msg": "Something went wrong!" }, 500
    
