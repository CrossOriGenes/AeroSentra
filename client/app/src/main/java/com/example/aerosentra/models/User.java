package com.example.aerosentra.models;

public class User {
    private String userId;
    private String username;
    private String email;
    private String photoUrl;
    private String fcmToken;

    public User(String userId, String username, String email, String photoUrl, String fcmToken) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.photoUrl = photoUrl;
        this.fcmToken = fcmToken;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

}
