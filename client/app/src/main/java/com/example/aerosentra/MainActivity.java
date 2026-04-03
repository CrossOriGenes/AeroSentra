package com.example.aerosentra;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.aerosentra.utils.AnalyticsFragment;
import com.example.aerosentra.utils.HomeFragment;
import com.example.aerosentra.utils.MapFragment;
import com.example.aerosentra.utils.SettingsFragment;


public class MainActivity extends AppCompatActivity {

    CardView navbar;
    ImageView homeIcon, mapIcon, analyticsIcon, settingsIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navbar = findViewById(R.id.bottom_nav);
        homeIcon = findViewById(R.id.nav_home);
        mapIcon = findViewById(R.id.nav_maps);
        analyticsIcon = findViewById(R.id.nav_analytics);
        settingsIcon = findViewById(R.id.nav_settings);

        select(homeIcon);
        loadFragment(new HomeFragment());

        homeIcon.setOnClickListener(v -> {
            select(homeIcon);
            loadFragment(new HomeFragment());
        });
        mapIcon.setOnClickListener(v -> {
            select(mapIcon);
            loadFragment(new MapFragment());
        });
        analyticsIcon.setOnClickListener(v -> {
            select(analyticsIcon);
            loadFragment(new AnalyticsFragment());
        });
        settingsIcon.setOnClickListener(v -> {
            select(settingsIcon);
            loadFragment(new SettingsFragment());
        });
    }

    private void select(ImageView item) {
        homeIcon.setSelected(false);
        mapIcon.setSelected(false);
        analyticsIcon.setSelected(false);
        settingsIcon.setSelected(false);

        item.setSelected(true);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        item.startAnimation(fadeIn);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}