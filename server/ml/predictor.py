import joblib, json
import pandas as pd
from pathlib import Path
from huggingface_hub import hf_hub_download

BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "model.pkl"
FEATURES_PATH = BASE_DIR / "feature_list.json"

_model = None
FEATURES = None
def get_model():
    global _model
    if _model is None:
        try:
            print("🌐 Loading model from Hugging Face...")
            path = hf_hub_download(
                repo_id="Snehodipto14/AeroSentra",
                filename="model.pkl",
                repo_type="model"
            )
            _model = joblib.load(path)
        except Exception:
            print("📂 Loading local model...")
            _model = joblib.load(MODEL_PATH)
    return _model

def get_features():
    global FEATURES

    if FEATURES is None:
        try:
            print("🌐 Loading features from Hugging Face...")
            path = hf_hub_download(
                repo_id="Snehodipto14/AeroSentra",
                filename="feature_list.json",
                repo_type="model"
            )
            with open(path) as f:
                FEATURES = json.load(f)
        except Exception:
            print("📂 Loading local features...")
            with open(FEATURES_PATH) as f:
                FEATURES = json.load(f)

    return FEATURES
    

label_map = {
    0: "GOOD",
    1: "MODERATE",
    2: "POOR"
}

def predict(payload: dict):
    features = get_features()
    model = get_model()
    input_vector = {fe: float(payload[fe]) for fe in features}
    x = pd.DataFrame([input_vector], columns=features)
    label = int(model.predict(x)[0])

    return {
        "class": label,
        "status": label_map[label]
    }
