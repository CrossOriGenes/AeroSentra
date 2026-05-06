from fastapi import APIRouter, UploadFile, File, Form
from datetime import datetime, UTC
from schema.reqresmodels import UserPayload
from services.config.firebase_config import db
import cloudinary.uploader

router = APIRouter()


# Create account
@router.post("/register_user")
def user_signup(payload: UserPayload):
    try:
        u_name = payload.username
        email = payload.email
        u_id = payload.userId
        device_id = payload.deviceId
        fcm_token = payload.fcmToken

        if not u_name or not email or not u_id or not device_id or not fcm_token:
            return { 
               "success": False, 
               "message": "Parameters missing!" 
            }
            
        doc_id = device_id
        user_ref = db.collection("Users").document(doc_id)
        user = user_ref.get()
        now = datetime.now(UTC)
        if user.exists:
            user_ref.update({
                "fcmToken": fcm_token,
                "userId": u_id,
                "username": u_name,
                "email": email,
                "updatedAt": now 
            })
        else:
            user_ref.set({
                "deviceId": device_id,
                "userId": u_id,
                "username": u_name,
                "email": email,
                "fcmToken": fcm_token,
                "role": "user",
                "createdAt": now
            })            
        
        
        return {
            "success": True,
            "message": "Account created successfully"
        }        
    except Exception as e:
        print(str(e))
        return { 
            "success": False, 
            "msg": "Something went wrong!" 
        }
    

# User log in
@router.get("/login")
def user_login(device_id: str, fcm_token: str):
    try:        
        doc_id = device_id
        user_ref = db.collection("Users").document(doc_id)
        user = user_ref.get()
        if user.exists:
            now = datetime.now(UTC)
            user_ref.update({
                "fcmToken": fcm_token,                
                "updatedAt": now  
            })
            username = user.get("username")
        
        return {
            "success": True,
            "message": f"Hi👋🏻 {username}, Welcome..."
        }
    except Exception as e:
        print(str(e))
        return { 
            "success": False, 
            "msg": "Something went wrong!" 
        }
        

# User data update
@router.put("/update_profile/{deviceId}")
async def update_profile(
    deviceId: str,
    username: str = Form(...),
    email: str = Form(...),
    profile: UploadFile = File(None)
):
    try:
        user_ref = db.collection("Users").document(deviceId)
        user = user_ref.get()
        if not user.exists:
            return {
                "success": False,
                "message": "User not found!"
            }
        
        old_data = user.to_dict()      
        image_url = old_data.get("photo_url", "")
        image_id = old_data.get("photo_id", "")
        
        if profile:
            if image_id:
                cloudinary.uploader.destroy(image_id)
            upload_result = cloudinary.uploader.upload(
                profile.file,
                folder="AeroSentra"
            )
            image_url = upload_result.get("secure_url")
            image_id = upload_result.get("public_id")
            
        user_ref.update({
            "username": username,
            "email": email,
            "photoUrl": image_url,
            "photo_id": image_id,
            "updatedAt": datetime.now(UTC) 
        })
        
        return {
            "success": True,
            "message": "Data updated successfully",
            "user": {
                "userId": old_data.get("userId", ""),
                "username": username,
                "email": email,
                "photoUrl": image_url
            }
        }
            
    except Exception as e:
        print(str(e))
        return { 
            "success": False, 
            "msg": "Something went wrong!" 
        }
        

# Delete account
@router.delete("/delete_account/{deviceId}")
async def delete_account(deviceId: str):
    try:
        doc_id = deviceId
        user_ref = db.collection("Users").document(doc_id)
        user = user_ref.get()
        if not user.exists:
            return {
                "success": False,
                "message": "User not found"
            }
        
        old_data = user.to_dict()      
        image_id = old_data.get("photo_id", "")
        if image_id:
            cloudinary.uploader.destroy(image_id)
        user_ref.delete()
        
        return {
            "success": True,
            "message": "Account deleted"
        }        
    except Exception as e:
        print(str(e))
        return { 
            "success": False, 
            "msg": "Something went wrong!" 
        }
        