package com.example.aerosentra;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.RoverAPIService;
import com.example.aerosentra.models.RoverStatus;
import com.example.aerosentra.models.response.TriggerResponse;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.Toaster;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.net.InetAddress;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoverTriggerActivity extends AppCompatActivity {

    TextView tvConnStatus, tvRoverDeviceName, tvRoverDeviceAddress;
    Button triggerCallBtn;
    AppCompatButton pairBtn;
    LinearLayout connectedDeviceDetailsGroup;
    ImageView statIcon;
    ImageView roverModel;

    PopupUtils loader;
    String status = "";
    RoverAPIService roverApi;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rover_trigger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        roverApi = APIClient.getMDNSClient().create(RoverAPIService.class);

        loader = new PopupUtils();
        triggerCallBtn = findViewById(R.id.trigger_call_button);
        pairBtn = findViewById(R.id.pair_button);
        tvConnStatus = findViewById(R.id.pair_stat_txt);
        tvRoverDeviceName = findViewById(R.id.rover_name);
        tvRoverDeviceAddress = findViewById(R.id.rover_ip_address);
        statIcon = findViewById(R.id.pair_stat_icon);

        connectedDeviceDetailsGroup = findViewById(R.id.connected_device_details);
        connectedDeviceDetailsGroup.setVisibility(View.GONE);

        if (!isHotspotEnabled()) {
            connectedDeviceDetailsGroup.setVisibility(View.GONE);
            showDisconnectedState();
        }

        pairBtn.setOnClickListener(v -> {
            if (!isHotspotEnabled()) startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            else startPairing();
        });
        triggerCallBtn.setOnClickListener(v -> triggerRover());


        roverModel = findViewById(R.id.roverModel);
        Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.float_anim);
        roverModel.startAnimation(floatAnim);

        View glow = findViewById(R.id.rover_bg_glow);
        ObjectAnimator glowAnim = ObjectAnimator.ofFloat(glow, "scaleX", 1f, 1.15f);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setDuration(2000);
        ObjectAnimator glowAnimY = ObjectAnimator.ofFloat(glow, "scaleY", 1f, 1.15f);
        glowAnimY.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimY.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimY.setDuration(2000);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(glowAnim, glowAnimY);
        set.start();

        findViewById(R.id.cancel_action_btn).setOnClickListener(v -> {
            startActivity(new Intent(RoverTriggerActivity.this, DashboardActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isHotspotEnabled()) startPairing();
    }


    private void showDisconnectedState() {
        status = "Disconnected";
        tvConnStatus.setText(status);
        statIcon.clearAnimation();
        statIcon.setImageResource(R.drawable.ic_wifi_off);
        statIcon.setColorFilter(Color.RED);
        pairBtn.setEnabled(true);
        pairBtn.setAlpha(1f);
        triggerCallBtn.setEnabled(false);
        triggerCallBtn.setAlpha(0.5f);
    }

    private void showConnectedState(String ip, String deviceName, String deviceStatus) {
        Toaster.success(RoverTriggerActivity.this, deviceStatus);
        connectedDeviceDetailsGroup.setVisibility(View.VISIBLE);
        status = "Connected";
        tvConnStatus.setText(status);
        statIcon.clearAnimation();
        statIcon.setColorFilter(Color.GREEN);
        pairBtn.setVisibility(View.GONE);
        triggerCallBtn.setEnabled(true);
        triggerCallBtn.setAlpha(1f);
        String deviceAddress = "IP -  ["+ ip +"]";
        tvRoverDeviceAddress.setText(deviceAddress);
        tvRoverDeviceName.setText(deviceName);
    }

    private void startPairing() {
        Toaster.info(this, "Please keep your hotspot ON to connect rover");
        status = "Pairing";
        tvConnStatus.setText(status);
        statIcon.setImageResource(R.drawable.ic_wifi);
        statIcon.setColorFilter(Color.YELLOW);
        startPulseAnimation();
        pairBtn.setEnabled(false);
        pairBtn.setAlpha(0.4f);
        checkRover();
    }

    private void checkRover() {
        roverApi.checkStatus().enqueue(new Callback<RoverStatus>() {
            @Override
            public void onResponse(Call<RoverStatus> call, Response<RoverStatus> response) {
                if (response.isSuccessful()) {
                    RoverStatus data = response.body();
                    String ip = data.getAddress();
                    String deviceName = data.getDevice();
                    String status = data.getStatus();
                    showConnectedState(ip, deviceName, status);
                }
                else {
                    try {
                        String error = response.errorBody().string();
                        JSONObject obj = new JSONObject(error);
                        String message = obj.getString("message");
                        Toaster.error(RoverTriggerActivity.this, message);
                    } catch (Exception e) {
                        Toaster.error(RoverTriggerActivity.this, "Unknown Error");
                    }
                }
            }

            @Override
            public void onFailure(Call<RoverStatus> call, Throwable t) {
                Toaster.error(RoverTriggerActivity.this, "Network Error: "+t.getMessage());
                showDisconnectedState();
            }
        });
    }

    private void startPulseAnimation() {
        AlphaAnimation pulse = new AlphaAnimation(0.4f, 1f);
        pulse.setDuration(700);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        statIcon.startAnimation(pulse);
    }

    private boolean isHotspotEnabled() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            int ipInt = wm.getConnectionInfo().getIpAddress();
            byte[] bytes = {
                    (byte) (ipInt & 0xFF),
                    (byte) (ipInt >> 8 & 0xFF),
                    (byte) (ipInt >> 16 & 0xFF),
                    (byte) (ipInt >> 24 & 0xFF)
            };
            InetAddress inetAddress = InetAddress.getByAddress(bytes);
            String ip = inetAddress.getHostAddress();
            Log.d("PHONE_IP", ip);
            return ip != null;
        } catch (Exception e) {
            Log.e("WIFI_CONNECTION_ERROR", e.toString());
            return false;
        }
    }

    private void triggerRover()  {
        loader.showLoader(this, "Getting earth data...");

        roverApi.triggerRover().enqueue(new Callback<TriggerResponse>() {
            @Override
            public void onResponse(Call<TriggerResponse> call, Response<TriggerResponse> response) {
                loader.dismiss();

                if (response.isSuccessful() && response.body()!=null) {
                    TriggerResponse res = response.body();

                    if (res.isSuccess()) {
                        Toaster.success(RoverTriggerActivity.this, res.getMsg());

                        Gson gson = new Gson();
                        String json = gson.toJson(res.getData());

                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("weather_data", json);
                        editor.apply();

                        startActivity(new Intent(RoverTriggerActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        try {
                            String error = response.errorBody().string();
                            JSONObject obj = new JSONObject(error);
                            String message = obj.getString("message");

                            Toaster.error(RoverTriggerActivity.this, message);
                        } catch (Exception e) {
                            Toaster.error(RoverTriggerActivity.this, "Unknown Error");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<TriggerResponse> call, Throwable t) {
                loader.dismiss();
                Toaster.error(RoverTriggerActivity.this, "Network Error: "+t.getMessage());
            }
        });
    }


}