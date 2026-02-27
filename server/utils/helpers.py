import os
import pandas as pd
from dotenv import load_dotenv
from huggingface_hub import hf_hub_download
from pydantic import BaseModel

load_dotenv()

        
# model-features engineering
FEATURES = [
    "temperature",
    "humidity",
    "pressure",
    "wind_speed",
    "hour",
    "day",
    "month"
]

# score categorizations
def categorize(score):
    if score >= 65:
        return 2  # POOR
    elif score >= 35:
        return 1  # MODERATE
    else:
        return 0  # GOOD

class RoverDevicePayload(BaseModel):
    temperature: float
    humidity: float
    aqi: int
    lat: float
    lon: float