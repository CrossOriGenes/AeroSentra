package com.example.aerosentra;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.WeatherAPIService;
import com.example.aerosentra.models.requests.GetWeatherDataRequest;
import com.example.aerosentra.models.response.TriggerResponse;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.Toaster;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    ImageView roverIcon;
    Button goToTriggerRoverActivityBtn;
    LinearLayout currentWeatherDetailsBtn;
    TextView card2_latLng, card2_place, card2_temp, card2_dnText;
    ImageView getMyCurrLocBtn, card2_dnIcon;

    SharedPreferences prefs;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    PopupUtils loader, popup;
    WeatherAPIService api;
    TriggerResponse.Data data;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String weatherData = prefs.getString("weather_data", "");
        double lat = Double.parseDouble(prefs.getString("lat", "0"));
        double lon = Double.parseDouble(prefs.getString("lon", "0"));


        loader = new PopupUtils();
        popup = new PopupUtils();

        api = APIClient.getServerClient().create(WeatherAPIService.class);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        roverIcon = findViewById(R.id.roverIcon);
        Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.float_anim);
        if (roverIcon != null) {
            roverIcon.startAnimation(floatAnim);
        }

        goToTriggerRoverActivityBtn = findViewById(R.id.goToTriggerRoverActivityBtn);
        goToTriggerRoverActivityBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RoverTriggerActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });


        card2_latLng = findViewById(R.id.card2_latLng);
        card2_place = findViewById(R.id.card2_place);
        card2_temp = findViewById(R.id.card2_temp);
        card2_dnText = findViewById(R.id.card2_dnText);
        card2_dnIcon = findViewById(R.id.card2_dnIcon);

        currentWeatherDetailsBtn = findViewById(R.id.seeCurrentWeatherDetailsBtn);
        getMyCurrLocBtn = findViewById(R.id.getMyCurrentLocationBtn);

        if (weatherData.isEmpty() || (lat==0 && lon==0)) {
            String valCoords = "__, __";
            String valPlace = "N.A.";
            String valTemp = "N.A.";
            String valDayNight = "N.A.";
            card2_latLng.setText(valCoords);
            card2_place.setText(valPlace);
            card2_temp.setText(valTemp);
            card2_dnText.setText(valDayNight);
            currentWeatherDetailsBtn.setVisibility(View.GONE);
        } else setDataToCard();


        getMyCurrLocBtn.setOnClickListener(v -> {
            if (!weatherData.isEmpty()) {
                popup.showConfirmationPopup(
                        this,
                        "Re-fetch?",
                        "Do you want to refetch the data using your current location?",
                        () -> {
                            prefs.edit().remove("weather_data").apply();
                            prefs.edit().remove("lat").apply();
                            prefs.edit().remove("lon").apply();
                            checkPermission();
                        }
                );
            } else checkPermission();
        });

        currentWeatherDetailsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });



        findViewById(R.id.card_redirect_to_alerts).setOnClickListener(v -> {
           Toaster.info(this, "Upcoming Alerts requested...");
        });
        findViewById(R.id.card_redirect_to_preventions).setOnClickListener(v -> {
            Toaster.info(this, "Upcoming Preventions requested...");
        });
        findViewById(R.id.seeNewsltrAndSubsDetailsBtn).setOnClickListener(v -> {
            Toaster.info(this, "Newsletter and Subscriptions details requested...");
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getLocation();
            else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                    openSettings();
                else
                    Toaster.warning(this, "Location permission required!");
            }
        }
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            getLocation();
        else
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
    }
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                Log.d("Location", "Lat: " + lat + ", Lon: " + lon);
                saveLocation(lat, lon);
                getCurrentWeather(lat, lon);
            } else {
                Toaster.error(this, "Unable to fetch location!");
            }
        });
    }
    private void saveLocation(double lat, double lon) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lat", String.valueOf(lat));
        editor.putString("lon", String.valueOf(lon));
        editor.apply();
    }

    private void getCurrentWeather(double lat, double lon) {
        loader.showLoader(this, "Getting weather of your place...");

        GetWeatherDataRequest req = new GetWeatherDataRequest((float)lat, (float)lon);
        api.getWeatherData(req).enqueue(new Callback<TriggerResponse>() {
            @Override
            public void onResponse(Call<TriggerResponse> call, Response<TriggerResponse> response) {
                loader.dismiss();
                if (response.isSuccessful() && response.body()!=null) {
                    TriggerResponse res = response.body();

                    if (res.isSuccess()) {
                        Log.d("SUCCESS_MESSAGE", res.getMsg());

                        Gson gson = new Gson();
                        String json = gson.toJson(res.getData());
                        Log.d("JSON_DATA", json);
                        prefs.edit().putString("weather_data", json).apply();
                        Log.d("PREFERENCES_SUCCESS_STORE_MESSAGE", "Data stored in preferences successfully");
                        setDataToCard();
                    }
                } else {
                    try {
                        String error = response.errorBody().toString();
                        JSONObject obj = new JSONObject(error);
                        String message = obj.getString("message");
                        Toaster.error(DashboardActivity.this, message);
                    } catch (Exception e) {
                        Toaster.error(DashboardActivity.this, "Unknown Error");
                    }
                }
            }
            @Override
            public void onFailure(Call<TriggerResponse> call, Throwable t) {
                loader.dismiss();
                Toaster.error(DashboardActivity.this, "Network Error: "+t.getMessage());
            }
        });
    }

    private void setDataToCard() {
        String json = prefs.getString("weather_data", "");
        if (json.isEmpty()) return;
        Gson gson = new Gson();
        data = gson.fromJson(json, TriggerResponse.Data.class);
        double lat = data.getApi_data().getCoords().getLat();
        double lon = data.getApi_data().getCoords().getLng();
        String latDir = lat > 0 ? "°N" : "°S";
        String lonDir = lon > 0 ? "°E" : "°W";
        String valCoords = String.format("%.4f%s, %.4f%s", lat, latDir, lon, lonDir);
        String valPlace = data.getApi_data().getCity_name();
        String valTemp = Math.round(data.getApi_data().getTemp())+"°C";
        boolean isDay = data.getApi_data().getIs_day();
        card2_latLng.setText(valCoords);
        card2_place.setText(valPlace);
        card2_temp.setText(valTemp);
        card2_dnText.setText(isDay ? "Day" : "Night");
        card2_dnIcon.setImageResource(isDay ? R.drawable.ic_sun : R.drawable.ic_moon);
        card2_dnIcon.setImageTintList(isDay ?
                ColorStateList.valueOf(Color.parseColor("#ff9900")) :
                ColorStateList.valueOf(Color.parseColor("#0099ff"))
        );
        currentWeatherDetailsBtn.setVisibility(View.VISIBLE);
    }
}