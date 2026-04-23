from fastapi import APIRouter
from datetime import datetime, UTC
from schema.reqresmodels import *
from ml.predictor import predict
from services.weather_service import *
from services.image_service import attach_images
from services.push_service import send_push
from services.config.firebase_config import db
from services.config.genai_config import client
from utils.helpers import generate_weather_alert_brief, generate_weather_alert_details

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
    

# Generate alert push notification depending on recieved weather
@router.post("/weather_alert_notification")
async def sendAlertNotification(req: AlertPushPayload):
    try:
        token = req.fcmToken
        data = req.data
        deviceId = req.deviceId
        userId = req.userId
                
        if not token or not data or not deviceId or not userId:
            return {
                "success": False,
                "message": "Parameters missing!"
            }
        
        doc_id = deviceId
        user_ref = db.collection("Users").document(doc_id)
        user = user_ref.get()
        if user.exists:
            user_ref.update({
                "fcmToken": token,
                "last_alerted_at": datetime.now(UTC),
                "userId": userId
            })
        else:
            now = datetime.now(UTC)
            user_ref.set({
                "userId": userId,
                "deviceId": deviceId,
                "fcmToken": token,
                "role": "guest" if "guest" in userId else "user",
                "createdAt": now,
                "last_alerted_at": now,
            })            
        
        alert = await generate_weather_alert_brief(data)
        # send push message alert
        send_push(token, alert["title"], alert["body"])
        
        return {
            "success": True,
            "message": "Test Successful"
        }        
    except Exception as e:
        print(str(e))
        return { 
            "success":False, 
            "msg": "Something went wrong!" 
        }


# generate detailed alert report
@router.post("/alert_report_details")
async def sendAlertReportDetails(data: dict):
    try:
        if not data:
            return {
                "success": False,
                "message": "Parameters missing!"
            } 
            
        result = await generate_weather_alert_details(data)        
        return {
            "success": True,
            "report": result            
        }
    except Exception as e:
        print(str(e))
        return { 
            "success":False, 
            "msg": "Something went wrong!" 
        }


# test
@router.get("/test")
async def test():
    try:
        model_names = [m.name for m in client.models.list()]
        print(model_names)
        
        return {
            "models": model_names
        }     
    except Exception as e:
        print(str(e))
        return { 
            "success":False, 
            "msg": "Something went wrong!" 
        }