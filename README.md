
# **AeroSentra ⛈️🤖**  
> Intelligent Autonomous Rover System with Android Control Interface

AeroSentra is an integrated robotics platform combining:

- 📱 Android Client Application
- ⚡ FastAPI Backend Server
- 🤖 ESP-based Rover Hardware

Designed as a modular, scalable system for real-time rover control, monitoring, and intelligent extensions.

---
## **🏗️ Project Architecture**
### `AeroSentra`(root)
- #### `hardware/` →  ESP firmware (.ino)
- #### `client/` →  Android application
- #### `server/` →  FastAPI backend (API layer)

---

## **📱 Android Client**

- Modern Material UI
- Custom Typography (Jost)
- Lottie animations
- Adaptive Launcher Icon
- Modular architecture
- Future-ready ML integration

---

## **🌐 Backend (FastAPI)**

- RESTful API
- Hardware communication layer
- Scalable microservice-ready structure

### **Installation (at "/AeroSentra/server")**
```bash
  python -m venv venv
  venv\Scripts\activate
  pip install -r requirements.txt
  uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```

---

## **🤖 Hardware**

- ESP-based rover firmware
- Modular motor driver integration
- Expandable sensor support

---

## **🎯 Vision**

AeroSentra aims to bridge:

Mobile UI → Intelligent Backend → Embedded Hardware

Creating a scalable robotics ecosystem for research and innovation.

---

## **📜 License**

MIT License © 2026