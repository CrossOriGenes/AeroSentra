from fastapi import FastAPI
from routes.weather import router as weather_router
from routes.user import router as users_router

app = FastAPI(title="AeroSentra")

app.include_router(weather_router, prefix="/api/weather")
app.include_router(users_router, prefix="/api/user")

@app.get("/healthz")
def health():
    return "Server Running OK ✅"

@app.get("/")
def root():
    return { 
        "status": 200, 
        "message": "Server Running 🚀"
    }
