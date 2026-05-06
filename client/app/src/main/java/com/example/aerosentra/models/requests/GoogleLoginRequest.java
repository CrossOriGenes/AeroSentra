package com.example.aerosentra.models.requests;

public class GoogleLoginRequest {
    private final String deviceId;
    private final String googleId;
    private final String username;
    private final String email;
    private final String photoUrl;
    private final String fcmToken;

    public GoogleLoginRequest(String deviceId, String googleId, String username, String email, String photoUrl, String fcmToken) {
        this.deviceId = deviceId;
        this.googleId = googleId;
        this.username = username;
        this.email = email;
        this.photoUrl = photoUrl;
        this.fcmToken = fcmToken;
    }

    public String getDeviceId() { return deviceId; }
    public String getGoogleId() { return googleId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public String getFcmToken() { return fcmToken; }

}
