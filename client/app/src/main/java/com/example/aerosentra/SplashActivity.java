package com.example.aerosentra;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.UUID;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean hasSeenIntro;
        try {
            hasSeenIntro = prefs.getBoolean("hasSeenIntro", false);
        } catch (ClassCastException e) {
            // Handle legacy String value if it exists from older versions
            String legacyValue = prefs.getString("hasSeenIntro", "false");
            hasSeenIntro = "true".equalsIgnoreCase(legacyValue);
            // Migrate to boolean for future consistency
            prefs.edit().putBoolean("hasSeenIntro", hasSeenIntro).apply();
        }

        String userId = prefs.getString("user_id", "");
        if (userId.isEmpty()) {
            String newUserId = "guest_" + UUID.randomUUID().toString().substring(0,8);
            prefs.edit().putString("user_id", newUserId).apply();
        }
        String deviceId = prefs.getString("device_id", "");
        if (deviceId.isEmpty()) {
            deviceId = Settings.Secure.getString(
                    getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            prefs.edit().putString("device_id", deviceId).apply();
        }


        // Splash delay (3 seconds)
        boolean finalHasSeenIntro = hasSeenIntro;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!finalHasSeenIntro) {
                startActivity(new Intent(SplashActivity.this, IntroActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
            }
            finish();
        },3000);

    }
}