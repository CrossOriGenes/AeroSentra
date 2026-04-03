package com.example.aerosentra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.ui.Toaster;

public class SplashActivity extends AppCompatActivity {

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
        boolean hasSeenIntro = prefs.getBoolean("hasSeenIntro", false);

        // Splash delay (3 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!hasSeenIntro)
                startActivity(new Intent(SplashActivity.this, IntroActivity.class));
            else
                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
            finish();
        },3000);

    }
}