package com.example.aerosentra.services;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.WeatherAPIService;
import com.example.aerosentra.models.requests.GetWeatherDataRequest;
import com.example.aerosentra.models.response.WeatherDataResponse;
import com.example.aerosentra.utils.CustomNotifications;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Response;

public class DataExpiryWorker extends Worker {
    SharedPreferences prefs;
    WeatherAPIService api;


    public DataExpiryWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            api = APIClient.getServerClient().create(WeatherAPIService.class);

            prefs = getApplicationContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
            long lastFetchTime = prefs.getLong("fetch_time", 0);
            long now = System.currentTimeMillis();
            long diff = now - lastFetchTime;
            long expiry = 30 * 60 * 1000;
            if (diff > expiry) {
                // re-fetch new weather data in background
                double lat = Double.parseDouble(prefs.getString("lat", "0"));
                double lon = Double.parseDouble(prefs.getString("lon", "0"));
                fetchCurrentWeatherDataOnBgTask(lat, lon);
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("WORKER_ERROR", "Worker Error: " + e.getMessage());
            return Result.retry();
        }
    }

    @SuppressLint("HardwareIds")
    private void fetchCurrentWeatherDataOnBgTask(double lat, double lon) {
        try {
            String deviceId = prefs.getString("device_id", "");
            if (deviceId.isEmpty()) {
                deviceId = Settings.Secure.getString(
                        getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );
                prefs.edit().putString("device_id", deviceId).apply();
            }
            String userId = prefs.getString("user_id", "");

            CustomNotifications alertPush = new CustomNotifications(deviceId, userId);
            GetWeatherDataRequest req = new GetWeatherDataRequest((float) lat, (float) lon);
            Call<WeatherDataResponse> call = api.getWeatherData(req);
            Response<WeatherDataResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                WeatherDataResponse res = response.body();
                if (res.isSuccess()) {
                    Log.d("SUCCESS_MESSAGE", res.getMsg());

                    prefs.edit()
                            .remove("weather_data")
                            .remove("nearby_places")
                            .remove("lat")
                            .remove("lon")
                            .remove("fetch_time")
                            .remove("alert_report")
                            .remove("alert_generated_at")
                            .apply();
                    WeatherDataResponse.Data data = res.getData();
                    Gson gson = new Gson();
                    String json = gson.toJson(data);
                    prefs.edit()
                            .putString("weather_data", json)
                            .putString("lat", String.valueOf(lat))
                            .putString("lon", String.valueOf(lon))
                            .putLong("fetch_time", System.currentTimeMillis())  // ✅ timestamp update
                            .apply();
                    Log.d("WEATHER_DATA_BG_FETCH_SUCCESS", "New weather data fetched and saved");
                    // send update broadcast for refreshing
                    Intent intent = new Intent("WEATHER_DATA_UPDATED");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    /* send new alert report push notification again
                    * only if previous was sent 1hr ago
                    * */
                    long lastAlertedTime = prefs.getLong("alert_generated_at", 0);
                    long now = System.currentTimeMillis();
                    long diff = now - lastAlertedTime;
                    long expiry = 60 * 60 * 1000;
                    if (diff > expiry) alertPush.getAnalyticReportPush(data);
                }
            } else Log.e("WEATHER_DATA_BG_FETCH_FAILURE", "Response unsuccessful");

        } catch (Exception e) {
            Log.e("NETWORK_ERROR", "Network Error: " + e.getMessage());
        }
    }
}