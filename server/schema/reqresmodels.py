from pydantic import BaseModel



# rover response model
class RoverDevicePayload(BaseModel):
    temperature: float
    humidity: float
    aqi: int
    lat: float
    lon: float
    
    
# mobile response model
class UiDevicePayload(BaseModel):
    lat: float
    lon: float
    
