package com.example.aerosentra.models.requests;

public class SignupRequest {
    private String username;
    private String email;
    private String deviceId;
    private String userId;
    private String fcmToken;
    private String photoUrl;

    public SignupRequest(String username, String email, String deviceId, String userId, String fcmToken, String photoUrl) {
        this.username = username;
        this.email = email;
        this.deviceId = deviceId;
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.photoUrl = photoUrl;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDeviceId() { return deviceId; }
    public String getUserId() { return userId; }
    public String getFcmToken() { return fcmToken; }
    public String getPhotoUrl() { return photoUrl; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

}
