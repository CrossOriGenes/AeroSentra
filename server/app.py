from fastapi import FastAPI
from socketio_server import app as sio_app

app = FastAPI(title="AeroSentra")

# app.include_router(api_router, prefix="/api")
app.mount("/socket.io", sio_app)
@app.get("/")
def root():
    return { 
        "status": 200, 
        "message": "Server Running 🚀"
    }
