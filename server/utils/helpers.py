import os
import pandas as pd
from dotenv import load_dotenv

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



# AQI EXTRACTORS
def interpolate(cp, blo, bhi, ilo, ihi):
    return ((ihi - ilo) / (bhi - blo)) * (cp - blo) + ilo

def calc_pm25(pm25):
    if pm25 <= 30:
        return interpolate(pm25, 0, 30, 0, 50)
    elif pm25 <= 60:
        return interpolate(pm25, 31, 60, 51, 100)
    elif pm25 <= 90:
        return interpolate(pm25, 61, 90, 101, 200)
    elif pm25 <= 120:
        return interpolate(pm25, 91, 120, 201, 300)
    elif pm25 <= 250:
        return interpolate(pm25, 121, 250, 301, 400)
    else:
        return interpolate(pm25, 251, 500, 401, 500)
    
def calc_pm10(pm10):
    if pm10 <= 50:
        return interpolate(pm10, 0, 50, 0, 50)
    elif pm10 <= 100:
        return interpolate(pm10, 51, 100, 51, 100)
    elif pm10 <= 250:
        return interpolate(pm10, 101, 250, 101, 200)
    elif pm10 <= 350:
        return interpolate(pm10, 251, 350, 201, 300)
    elif pm10 <= 430:
        return interpolate(pm10, 351, 430, 301, 400)
    else:
        return interpolate(pm10, 431, 600, 401, 500)
    
def calc_co(co):
    if co <= 1:
        return interpolate(co, 0, 1, 0, 50)
    elif co <= 2:
        return interpolate(co, 1.1, 2, 51, 100)
    elif co <= 10:
        return interpolate(co, 2.1, 10, 101, 200)
    elif co <= 17:
        return interpolate(co, 10.1, 17, 201, 300)
    elif co <= 34:
        return interpolate(co, 17.1, 34, 301, 400)
    else:    
        return interpolate(co, 34.1, 50, 401, 500)
    
    
def calc_no2(no2):
    if no2 <= 40:
        return interpolate(no2, 0, 40, 0, 50)
    elif no2 <= 80:
        return interpolate(no2, 41, 80, 51, 100)
    elif no2 <= 180:
        return interpolate(no2, 81, 180, 101, 200)
    elif no2 <= 280:
        return interpolate(no2, 181, 280, 201, 300)
    elif no2 <= 400:
        return interpolate(no2, 281, 400, 301, 400)
    else:
        return interpolate(no2, 401, 600, 401, 500)
    
def calc_o3(o3):
    if o3 <= 50:
        return interpolate(o3, 0, 50, 0, 50)
    elif o3 <= 100:
        return interpolate(o3, 51, 100, 51, 100)
    elif o3 <= 168:
        return interpolate(o3, 101, 168, 101, 200)
    elif o3 <= 208:
        return interpolate(o3, 169, 208, 201, 300)
    elif o3 <= 748:
        return interpolate(o3, 209, 748, 301, 400)
    else:
        return interpolate(o3, 749, 1000, 401, 500)
    
def calc_so2(so2):
    if so2 <= 40:
        return interpolate(so2, 0, 40, 0, 50)
    elif so2 <= 80:
        return interpolate(so2, 41, 80, 51, 100)
    elif so2 <= 380:
        return interpolate(so2, 81, 380, 101, 200)
    elif so2 <= 800:
        return interpolate(so2, 381, 800, 201, 300)
    elif so2 <= 1600:
        return interpolate(so2, 801, 1600, 301, 400)
    else:
        return interpolate(so2, 1601, 2000, 401, 500)