package com.example.aerosentra.workers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.WeatherAPIService;
import com.example.aerosentra.models.requests.GetWeatherDataRequest;
import com.example.aerosentra.models.response.WeatherDataResponse;
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

    private void fetchCurrentWeatherDataOnBgTask(double lat, double lon) {
        try {
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
                            .apply();
                    Gson gson = new Gson();
                    String json = gson.toJson(res.getData());
                    prefs.edit()
                            .putString("weather_data", json)
                            .putLong("fetch_time", System.currentTimeMillis())  // ✅ timestamp update
                            .apply();

                    Log.d("WEATHER_DATA_BG_FETCH_SUCCESS", "New weather data fetched and saved");
                }
            } else Log.e("WEATHER_DATA_BG_FETCH_FAILURE", "Response unsuccessful");

        } catch (Exception e) {
            Log.e("NETWORK_ERROR", "Network Error: " + e.getMessage());
        }
    }
}