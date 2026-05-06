from pydantic import BaseModel



# rover request payload model
class RoverDevicePayload(BaseModel):
    temperature: float
    humidity: float
    aqi: int
    lat: float
    lon: float
    
    
# mobile request payload model
class UiDevicePayload(BaseModel):
    lat: float
    lon: float
    

# Push message request payload
class AlertPushPayload(BaseModel):
    data: dict
    fcmToken: str
    deviceId: str
    userId: str
    
    
# User model
class UserPayload(BaseModel):
    fcmToken: str
    userId: str
    deviceId: str
    username: str
    email: str
    photoUrl: str | None 