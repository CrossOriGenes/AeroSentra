package com.example.aerosentra.models.requests;

import com.example.aerosentra.models.response.WeatherDataResponse;

public class PushMessageRequest {

    WeatherDataResponse.Data data;
    String fcmToken, deviceId, userId;

    public PushMessageRequest(WeatherDataResponse.Data data, String fcmToken, String deviceId, String userId) {
        this.data = data;
        this.fcmToken = fcmToken;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public WeatherDataResponse.Data getData() { return data; }
    public String getFcmToken() { return fcmToken; }
    public String getDeviceId() { return deviceId; }
    public String getUserId() { return userId; }

}
