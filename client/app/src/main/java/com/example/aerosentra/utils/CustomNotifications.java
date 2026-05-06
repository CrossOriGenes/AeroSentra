package com.example.aerosentra.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.WeatherAPIService;
import com.example.aerosentra.models.requests.PushMessageRequest;
import com.example.aerosentra.models.response.BasicResponse;
import com.example.aerosentra.models.response.WeatherDataResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomNotifications {

    WeatherDataResponse.Data data;
    String fcmToken, deviceId, userId;
    WeatherAPIService api;

    public CustomNotifications(String deviceId, String userId) {
        api = APIClient.getServerClient().create(WeatherAPIService.class);
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public void getAnalyticReportPush(WeatherDataResponse.Data data) {
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Gson gson = new Gson();
                        String json = gson.toJson(data);

                        if (token.isEmpty() || json == null) return;
                        this.fcmToken = token;
                        this.data = data;
                        Log.d("FCM_TOKEN", token);
                        Log.d("WEATHER_DATA", json);

                        sendForAnalysis();
                    } else {
                        Log.e("FCM_FAILED", "Failed to get FCM token and push report!");
                    }
                });
    }

    public void sendForAnalysis() {
        PushMessageRequest req = new PushMessageRequest(data, fcmToken, deviceId, userId);
        api.sendNotification(req).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse res = response.body();
                    if (res.isSuccess()) {
                        String msg = res.getMessage();
                        Log.d("SUCCESS_MESSAGE", msg != null ? msg : "Success (no message)");
                    }
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("ERROR", error);
                    } catch (Exception e) {
                        String errMsg = e.getMessage();
                        Log.e("ERROR_FETCHING_DATA", errMsg != null ? errMsg : "Exception with no message");
                    }
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                String msg = t.getMessage();
                Log.e("Network Error", msg != null ? msg : "Unknown failure");
            }
        });
    }
}
