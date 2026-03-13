from fastapi import FastAPI
from routes.weather import router as weather_router

app = FastAPI(title="AeroSentra")

app.include_router(weather_router, prefix="/api")

@app.get("/healthz")
def health():
    return "Server Running OK ✅"

@app.get("/")
def root():
    return { 
        "status": 200, 
        "message": "Server Running 🚀"
    }
