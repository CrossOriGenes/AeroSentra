package com.example.aerosentra;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.aerosentra.utils.PlanModel;
import com.example.aerosentra.utils.PricingAdapter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SubscriptionsActivity extends AppCompatActivity {

    ViewPager2 priceCardsPager;
    LinearLayout cardPositionDots;

    List<PlanModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subscriptions);

        priceCardsPager = findViewById(R.id.pricingCardsPager);
        cardPositionDots = findViewById(R.id.cardPositionDots);
        View indicator = findViewById(R.id.tabIndicator);
        TextView tvMonthly = findViewById(R.id.tvMonthly);
        TextView tvYearly = findViewById(R.id.tvYearly);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        findViewById(R.id.back).setOnClickListener(v -> {
            String source = getIntent().getStringExtra("source");
            if (source != null) {
                if (source.equalsIgnoreCase("home")) {
                    startActivity(new Intent(this, DashboardActivity.class));
                } else {
                    startActivity(new Intent(this, ProfileActivity.class));
                }
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            } else finish();
        });

        list = new ArrayList<>();
        initPricingCards();

        AtomicBoolean isYearly = new AtomicBoolean(false);

        PricingAdapter adapter = new PricingAdapter(this, list, isYearly.get());
        priceCardsPager.setAdapter(adapter);

        tvMonthly.setTextColor(Color.WHITE);
        tvYearly.setTextColor(Color.parseColor("#555555"));

        tvMonthly.setOnClickListener(v -> indicator.post(() -> {
           indicator.animate()
                   .translationX(0f)
                   .setDuration(250)
                   .setInterpolator(new DecelerateInterpolator())
                   .start();
           tvMonthly.setTextColor(Color.WHITE);
           tvYearly.setTextColor(Color.parseColor("#555555"));
           isYearly.set(false);
           adapter.setYearly(false);
        }));
        tvYearly.setOnClickListener(v -> indicator.post(() -> {
            indicator.animate()
                    .translationX(indicator.getWidth())
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            tvYearly.setTextColor(Color.WHITE);
            tvMonthly.setTextColor(Color.parseColor("#555555"));
            isYearly.set(true);
            adapter.setYearly(true);
        }));

        setupDots(list.size());
        priceCardsPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
           @Override
           public void onPageSelected(int position) {
               super.onPageSelected(position);
               updateDot(position);
           }
        });
    }

    private void initPricingCards() {
        try {
            InputStream is = getAssets().open("pricing.json");
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type type = new TypeToken<List<PlanModel>>() {}.getType();

            list.clear();
            list = gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e("PRICING_DATA_ERROR", e.getMessage());
        }
    }
    private void setupDots(int count) {
        cardPositionDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 18);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.rounded_pill);
            dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#555555")));
            cardPositionDots.addView(dot);
        }
    }
    private void updateDot(int position) {
        for (int i = 0; i < cardPositionDots.getChildCount(); i++) {
            View dot = cardPositionDots.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            if (i == position) {
                dot.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                params.width = 54;
            } else {
                dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#555555")));
                params.width = 32;
            }
            dot.setLayoutParams(params);
        }
    }
}