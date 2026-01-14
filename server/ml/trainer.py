import joblib, json, os
import pandas as pd
from tqdm import tqdm
from pathlib import Path
from dotenv import load_dotenv
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from huggingface_hub import HfApi,hf_hub_download
from utils.helpers import FEATURES, categorize

load_dotenv()
BASE_DIR = Path(__file__).resolve().parent
SERVER_DIR = BASE_DIR.parent
DATA_DIR = SERVER_DIR / "data"
LOCAL_CSV_PATH = DATA_DIR / "weather_dataset.csv"


def load_dataset():
    try:
        print("📥 Loading dataset from Hugging Face...")
        csv_path = hf_hub_download(
            repo_id="Snehodipto14/AeroSentra",
            filename="weather_dataset.csv",
            repo_type="model"
        )
        return pd.read_csv(csv_path)
    except Exception:
        print("📥 Loading dataset locally...")
        if not LOCAL_CSV_PATH.exists():
            raise FileNotFoundError(f"Local dataset not found at {LOCAL_CSV_PATH}")
        return pd.read_csv(LOCAL_CSV_PATH)        

df = load_dataset()
print(f"✅ Dataset loaded: {df.shape}")


print("🧮 Creating air_score...")
df["air_score"] = (
    0.3 * df["temperature"] +
    0.25 * df["humidity"] +
    0.25 * (1013 - df["pressure"]).abs() +
    0.2 * df["wind_speed"]
)

print("🧹 Selecting features & target...")
X = df[FEATURES].astype("float32")
y = df["air_score"].apply(categorize).astype("int")


print("📊 Class distribution:")
print(y.value_counts(normalize=True))


print("✂️ Train-test split starting...")
X_train, X_test, y_train, y_test = train_test_split(
    X, 
    y, 
    test_size=0.2, 
    random_state=42,
    shuffle=True
)


print(f"✅ Train size: {X_train.shape}, Test size: {X_test.shape}")


print("🌲 Initializing model...")
model = RandomForestClassifier(
    n_estimators=1,
    max_depth=22,
    min_samples_leaf=10,
    min_samples_split=20,
    max_features='sqrt',
    warm_start=True,
    n_jobs=-1,
    random_state=42
) 


print("🚀 Training started (this may take time)...")
for i in tqdm(range(1, 61)):
    model.n_estimators = i
    model.fit(X_train, y_train)


print("📊 Evaluating model...")
acc = model.score(X_test, y_test)
print(f"🎯 Validation accuracy: {acc:.4f}")


print("💾 Saving model & features...")
MODEL_PATH = BASE_DIR / "model.pkl"
joblib.dump(model, MODEL_PATH)

FEATURES_PATH = BASE_DIR / "feature_list.json"
with open(FEATURES_PATH, "w") as f:
    json.dump(FEATURES, f)
    
print("✅ Model & feature list saved")


HF_TOKEN = os.getenv('HF_TOKEN')
api = HfApi(token=HF_TOKEN)
try:
    # upload model
    api.upload_file(
        path_or_fileobj=str(MODEL_PATH),
        path_in_repo="model.pkl",
        repo_id="Snehodipto14/AeroSentra",
        repo_type="model",
    )
    # upload features.json
    api.upload_file(
        path_or_fileobj=str(FEATURES_PATH),
        path_in_repo="feature_list.json",
        repo_id="Snehodipto14/AeroSentra",
        repo_type="model",
    )
    print("Model & features successfully uploaded to ☁️.")
except Exception as e:
    print("❌ Failed to upload/overwrite model!\n", str(e))    