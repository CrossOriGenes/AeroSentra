package com.example.aerosentra;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.WeatherAPIService;
import com.example.aerosentra.models.requests.GetWeatherDataRequest;
import com.example.aerosentra.models.response.WeatherDataResponse;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.ProfileMenuBottomSheet;
import com.example.aerosentra.ui.Toaster;
import com.example.aerosentra.utils.CustomNotifications;
import com.example.aerosentra.utils.NotificationPermissionHelper;
import com.example.aerosentra.utils.SettingsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    ImageView roverIcon;
    Button goToTriggerRoverActivityBtn, goToPlansPage;
    ShapeableImageView profileBtn;
    LinearLayout currentWeatherDetailsBtn;
    TextView card2_latLng, card2_place, card2_temp, card2_dnText;
    ImageView getMyCurrLocBtn, card2_dnIcon;

    SharedPreferences prefs;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    PopupUtils loader, popup;
    WeatherAPIService api;
    WeatherDataResponse.Data data;



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
        loader = new PopupUtils();
        popup = new PopupUtils();
        api = APIClient.getServerClient().create(WeatherAPIService.class);

        checkNotificationPermission();

        String weatherData = prefs.getString("weather_data", "");
        double lat = Double.parseDouble(prefs.getString("lat", "0"));
        double lon = Double.parseDouble(prefs.getString("lon", "0"));


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
                            prefs.edit()
                                    .remove("weather_data")
                                    .remove("nearby_places")
                                    .remove("lat")
                                    .remove("lon")
                                    .remove("fetch_time")
                                    .remove("alert_report")
                                    .remove("alert_generated_at")
                                    .apply();
                            checkPermission();
                        }
                );
            } else checkPermission();
        });

        currentWeatherDetailsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });



        findViewById(R.id.card_redirect_to_preventions).setOnClickListener(v -> {
            startActivity(new Intent(this, PrecautionsAndAlertDetailsActivity.class));
            finish();
        });


        BottomSheetDialog sheet2 = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.subscription_enquiry_bottomsheet, null);
        sheet2.setContentView(view);
        view.findViewById(R.id.toSubscriptionsRedirectBtn).setOnClickListener(v1 -> {
           sheet2.dismiss();
              new Handler(Looper.getMainLooper()).postDelayed(() -> {
                  Intent i = new Intent(this, SubscriptionsActivity.class);
                  i.putExtra("source", "home");
                  startActivity(i);
                  finish();
              }, 200);
           });
        findViewById(R.id.card_redirect_to_alerts).setOnClickListener(v -> {
            String planType = prefs.getString("plan_type", "");
            if (planType.isEmpty()) sheet2.show();
        });


        goToPlansPage = findViewById(R.id.seeNewsltrAndSubsDetailsBtn);
        goToPlansPage.setOnClickListener(v -> {
            Intent i = new Intent(this, SubscriptionsActivity.class);
            i.putExtra("source", "home");
            startActivity(i);
            finish();
        });


        ProfileMenuBottomSheet sheet = getProfileMenuBottomSheet();
        profileBtn = findViewById(R.id.profileBtn);
        String photoUrl = prefs.getString("photo_url", "");
        if (photoUrl.isEmpty())
            profileBtn.setImageResource(R.drawable.user_dummy);
        else
            Glide.with(DashboardActivity.this)
                 .load(photoUrl)
                 .transition(DrawableTransitionOptions.withCrossFade(350))
                 .placeholder(R.drawable.user_dummy)
                 .error(R.drawable.user_dummy)
                 .into(profileBtn);
        profileBtn.setOnClickListener(v -> {
            sheet.show(getSupportFragmentManager(), "ProfileMenuBottomSheet");
        });
    }

    @NonNull
    private ProfileMenuBottomSheet getProfileMenuBottomSheet() {
        ProfileMenuBottomSheet sheet = new ProfileMenuBottomSheet();
        sheet.setListener(new ProfileMenuBottomSheet.OnActionListener() {
            @Override
            public void onLogout() {
                popup.showConfirmationPopup(
                        DashboardActivity.this,
                        "Confirm",
                        "Are you sure you want to logout from the app?",
                        () -> {
                            FirebaseAuth.getInstance().signOut();
                            prefs.edit()
                                    .remove("user_id")
                                    .remove("username")
                                    .remove("email")
                                    .remove("photo_url")
                                    .remove("fcm_token")
                                    .apply();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                finish();
                            }, 200);
                        }
                );
            }
            @Override
            public void onManageAccount() {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                finish();
                }, 200);
            }
            @Override
            public void onLoginRequest() {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                }, 200);
            }
        });
        return sheet;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(
                        weatherReceiver,
                        new IntentFilter("WEATHER_DATA_UPDATED")
                );
    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(weatherReceiver);
    }


    private void checkNotificationPermission() {
        if (NotificationPermissionHelper.isNotificationAllowed(this))
            return; // ✅ already allowed
        boolean askedBefore = prefs.getBoolean("notif_asked", false);
        if (!askedBefore) {
            prefs.edit().putBoolean("notif_asked", true).apply();
            showNotificationDialog();
        }
    }
    private void showNotificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Alerts 🔔")
                .setMessage("Get real-time alerts for UV, AQI and weather changes.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (NotificationPermissionHelper.isPermanentlyDenied(this)) {
                            // 🚫 Permanently denied → open settings
                            NotificationPermissionHelper.openSettings(this);
                        } else {
                            // ✅ First time → request permission
                            NotificationPermissionHelper.requestPermission(this);
                        }
                    }
                })
                .setNegativeButton("Not Now", null)
                .show();
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
        if (requestCode == NotificationPermissionHelper.REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NOTIFICATION_PERMISSION", "Notifications Enabled ✅");
            } else {
                Toaster.warning(this, "Notifications Disabled ❌");
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
                prefs.edit()
                        .putString("lat", String.valueOf(lat))
                        .putString("lon", String.valueOf(lon))
                        .apply();
                getCurrentWeather(lat, lon);
            } else {
                Toaster.error(this, "Unable to fetch location!");
            }
        });
    }

    private void getCurrentWeather(double lat, double lon) {
        loader.showLoader(this, "Getting weather of your place...");

        String deviceId = prefs.getString("device_id", "");
        String userId = prefs.getString("user_id", "");

        CustomNotifications alertPush = new CustomNotifications(deviceId, userId);

        GetWeatherDataRequest req = new GetWeatherDataRequest((float)lat, (float)lon);
        api.getWeatherData(req).enqueue(new Callback<WeatherDataResponse>() {
            @Override
            public void onResponse(Call<WeatherDataResponse> call, Response<WeatherDataResponse> response) {
                loader.dismiss();
                if (response.isSuccessful() && response.body()!=null) {
                    WeatherDataResponse res = response.body();

                    if (res.isSuccess()) {
                        Log.d("SUCCESS_MESSAGE", res.getMsg());

                        WeatherDataResponse.Data data = res.getData();
                        Gson gson = new Gson();
                        String json = gson.toJson(data);
                        prefs.edit()
                                .putString("weather_data", json)
                                .putLong("fetch_time", System.currentTimeMillis())
                                .apply();
                        setDataToCard();
                        alertPush.getAnalyticReportPush(data);
                    }
                } else {
                    try {
                        String error = response.errorBody().toString();
                        JSONObject obj = new JSONObject(error);
                        String message = obj.getString("message");
                        Toaster.error(DashboardActivity.this, message);
//                        Log.e("ERROR", message);
                    } catch (Exception e) {
                        Toaster.error(DashboardActivity.this, "Unknown Error");
//                        Log.e("ERROR_FETCHING_DATA", e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<WeatherDataResponse> call, Throwable t) {
                loader.dismiss();
                Toaster.error(DashboardActivity.this, "Network Error: "+t.getMessage());
                Log.e("Network Error", t.getMessage());
            }
        });
    }

    private void setDataToCard() {
        String json = prefs.getString("weather_data", "");
        if (json.isEmpty()) return;
        Gson gson = new Gson();
        try {
            data = gson.fromJson(json, WeatherDataResponse.Data.class);
        } catch (Exception e) {
            prefs.edit().remove("weather_data").apply();
            return;
        }
        if (data == null || data.getApi_data() == null) return;
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

    private final BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setDataToCard();
        }
    };

}