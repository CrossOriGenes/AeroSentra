import socketio
from ml.predictor import predict

sio = socketio.AsyncServer(
    async_mode="asgi",
    cors_allowed_origins="*"
)

app = socketio.ASGIApp(sio)

@sio.event
async def connect(sid, environ):
    print("🟢 Client connected:", sid)

@sio.event
async def disconnect(sid):
    print("🔴 Client disconnected:", sid)

@sio.event
async def predict_weather(sid, data):
    print("📥 Received:", data)
    result = predict(data)
    await sio.emit("prediction", result, to=sid)
