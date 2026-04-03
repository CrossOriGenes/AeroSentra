import os, json, firebase_admin
from firebase_admin import credentials, firestore
from dotenv import load_dotenv

load_dotenv()

FIREBASE_JSON = os.getenv("FIREBASE_SERVICE_JSON")
FIREBASE_KEY = json.loads(FIREBASE_JSON)


# verify auth certificate
cred = credentials.Certificate(FIREBASE_KEY)
# init app
firebase_admin.initialize_app(cred)


# database
db = firestore.client()
