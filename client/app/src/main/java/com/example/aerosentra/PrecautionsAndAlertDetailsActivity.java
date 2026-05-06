package com.example.aerosentra;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.WeatherAPIService;
import com.example.aerosentra.models.response.AlertReportDetailsResponse;
import com.example.aerosentra.models.response.WeatherDataResponse;
import com.example.aerosentra.ui.Toaster;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrecautionsAndAlertDetailsActivity extends AppCompatActivity {

    LinearLayout tvHeaderBtn, aqiValIndicatorPin, indicatorBar, reportContents;
    TextView tvSummary, tvDetails, tvAqiHeading, tvAqiVal, tvAqiStatVal, tvAqiStatText, tvUvHeading;
    ListView precautionsList, highlightsList;
    FrameLayout uvAlertMessageContainer;
    SpeedView uvGauge;
    View aqiStatDot;
    ShimmerFrameLayout skeleton;

    List<String> precautions, highlights;
    SharedPreferences prefs;
    WeatherDataResponse.Data data;
    AlertReportDetailsResponse.Report report;
    boolean isAqiAnimated = false, isUvAnimated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_precautions_and_alert_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String json = prefs.getString("weather_data", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            data = gson.fromJson(json, WeatherDataResponse.Data.class);
        }



        reportContents = findViewById(R.id.report_contents);
        skeleton = findViewById(R.id.alert_details_skeleton_loader);
        tvSummary = findViewById(R.id.alert_content_summary);
        tvDetails = findViewById(R.id.alert_content_details);
        precautionsList = findViewById(R.id.alert_content_precautions_list);
        highlightsList = findViewById(R.id.alert_content_highlights_list);

        String cachedReport = prefs.getString("alert_report", "");
        long lastTime = prefs.getLong("alert_generated_at", 0);
        long now = System.currentTimeMillis();
        boolean shouldFetch = true;
        if (!cachedReport.isEmpty() && lastTime > 0) {
            long diff = now - lastTime;
            if (diff < 1800000) shouldFetch = false;
        }
        if (shouldFetch) getDetailedReport();
        else initAlertDetailsCard();



        // AQI indicator
        tvAqiHeading = findViewById(R.id.aqi_label_heading);
        tvAqiHeading.setTextColor(Color.BLACK);
        aqiValIndicatorPin = findViewById(R.id.aqiValIndicatorPin);
        indicatorBar = findViewById(R.id.indicatorBar);
        tvAqiVal = aqiValIndicatorPin.findViewById(R.id.aqiValue);
        tvAqiStatVal = findViewById(R.id.aqiStatVal);
        tvAqiStatText = findViewById(R.id.aqiStatText);
        aqiStatDot = findViewById(R.id.aqiStatDot);
        loadAqiIndicator();



        // UV GAUGE
        uvGauge = findViewById(R.id.uvSpeedView);
        tvUvHeading = findViewById(R.id.uv_label_heading);
        uvAlertMessageContainer = findViewById(R.id.uvAlertContainer);
        Typeface semiBoldTf = ResourcesCompat.getFont(this, R.font.jost_semibold);
        if (semiBoldTf != null) {
            uvGauge.setSpeedTextTypeface(semiBoldTf);
        }
        tvUvHeading.setTextColor(Color.BLACK);
        loadUvRadial();



        tvHeaderBtn = findViewById(R.id.alert_details_header);
        tvHeaderBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    private void initAlertDetailsCard() {
        String alertJson = prefs.getString("alert_report", "");
        if (!alertJson.isEmpty()) {
            Gson gson = new Gson();
            AlertReportDetailsResponse.Report alertReport = gson.fromJson(alertJson, AlertReportDetailsResponse.Report.class);
            String summary = alertReport.getSummary();
            String details = alertReport.getDetails();
            precautions = new ArrayList<>();
            highlights = new ArrayList<>();
            if (alertReport.getPrecautions() != null)
                precautions.addAll(alertReport.getPrecautions());
            if (alertReport.getHighlights() != null)
                highlights.addAll(alertReport.getHighlights());
            if (alertReport.getPrecautions() == null &&
                    alertReport.getHighlights() == null) {
                findViewById(R.id.alert_detailed_report_card).setVisibility(View.GONE);
                findViewById(R.id.alert_details_fallback).setVisibility(View.VISIBLE);
                View aqiAnalyticSection = findViewById(R.id.aqi_analytic_section);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) aqiAnalyticSection.getLayoutParams();
                params.topMargin = 650;
                aqiAnalyticSection.setLayoutParams(params);
                return;
            }

            tvSummary.setText(summary);
            tvDetails.setText(details);
            ArrayAdapter<String> precautionsAdapter = new ArrayAdapter<>(
                    this,
                    R.layout.list_item_with_custom_bullet,
                    R.id.text,
                    precautions
            ){
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView bullet = view.findViewById(R.id.bullet);
                    bullet.setText("⚠️");
                    return view;
                }
            };
            precautionsList.setAdapter(precautionsAdapter);
            ArrayAdapter<String> highlightsAdapter = new ArrayAdapter<>(
                    this,
                    R.layout.list_item_with_custom_bullet,
                    R.id.text,
                    highlights
            ){
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView bullet = view.findViewById(R.id.bullet);
                    bullet.setText("✔");
                    bullet.setTextColor(Color.parseColor("#006600"));
                    return view;
                }
            };
            highlightsList.setAdapter(highlightsAdapter);
        } else {
            tvSummary.setText("N.A.");
            tvDetails.setText("N.A.");
            precautionsList.setAdapter(null);
            highlightsList.setAdapter(null);
        }
    }
    private void loadUvRadial() {
        TextView tvUVVal = findViewById(R.id.uvVal);
        TextView tvUVLvlStat = findViewById(R.id.uvLvlStat);
        View uvLvlStatDot = findViewById(R.id.uvLvlStatDot);
        LayoutInflater alertInflater = LayoutInflater.from(this);
        View alertView = alertInflater.inflate(R.layout.alert_box, null);
        TextView alertMsg = alertView.findViewById(R.id.alertMsg);

        double uvValue;
        if (data != null && data.getApi_data() != null) {
            double maxUv = 0.0;
            for (WeatherDataResponse.HourlyForecast hf : data.getApi_data().getHourly_forecast()) {
                if (hf.getUv() > maxUv) maxUv = hf.getUv();
            }
            uvValue = maxUv;
        } else uvValue = 0.0;
        uvGauge.invalidate();
        uvGauge.clearSections();
        uvGauge.addSections(
                new Section(0f, 0.18f, Color.GREEN, 35, Style.ROUND),    // 0 - 2
                new Section(0.18f, 0.45f, Color.YELLOW, 35, Style.ROUND),    // 2 - 5
                new Section(0.45f, 0.64f, Color.parseColor("#FFA500"), 35, Style.ROUND),  // 5 - 7
                new Section(0.64f, 0.90f, Color.RED, 35, Style.ROUND),   // 7 - 10
                new Section(0.90f, 1f, Color.parseColor("#660033"), 35, Style.ROUND)   // 10+
        );
        uvGauge.setUnitUnderSpeedText(true);
        uvGauge.setSpeedTextColor(Color.BLACK);
        uvGauge.getIndicator().setColor(Color.parseColor("#777777"));
        uvGauge.getViewTreeObserver().addOnDrawListener(() -> {
            Rect scrollBounds = new Rect();
            uvGauge.getHitRect(scrollBounds);
            if (!isUvAnimated && uvGauge.getLocalVisibleRect(scrollBounds)) {
                uvGauge.speedTo((float) uvValue, 1200);
                isUvAnimated = true;
            }
        });
        String uvStat = "";
        if (Math.round(uvValue) <= 2) {
            uvStat = "Low";
            uvLvlStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00E600")));
            uvAlertMessageContainer.setVisibility(View.GONE);
        } else if (Math.round(uvValue) <= 5) {
            uvStat = "Moderate";
            tvUVLvlStat.setTextSize(15);
            uvLvlStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
            uvAlertMessageContainer.setVisibility(View.GONE);
        } else if (Math.round(uvValue) <= 7) {
            uvStat = "High";
            uvLvlStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6600")));
            if (data.getApi_data().getIs_day()) {
                uvAlertMessageContainer.setVisibility(View.VISIBLE);
                alertMsg.setText(R.string.uvAlert_msg);
                uvAlertMessageContainer.addView(alertView);
            }
        } else if (Math.round(uvValue) <= 10) {
            uvStat = "Very High";
            tvUVLvlStat.setTextSize(14);
            uvLvlStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CC0000")));
            if (data.getApi_data().getIs_day()) {
                uvAlertMessageContainer.setVisibility(View.VISIBLE);
                alertMsg.setText(R.string.uvAlert_msg);
                uvAlertMessageContainer.addView(alertView);
            }
        } else {
            uvStat = "Extreme";
            uvLvlStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#660033")));
            if (data.getApi_data().getIs_day()) {
                uvAlertMessageContainer.setVisibility(View.VISIBLE);
                alertMsg.setText(R.string.uvAlert_msg);
                uvAlertMessageContainer.addView(alertView);
            }
        }
        tvUVLvlStat.setText(uvStat);
        tvUVVal.setText(String.format("%.1f", uvValue));
    }
    private void loadAqiIndicator() {
        double aqiVal = 0.0;
        if (data != null && data.getApi_data() != null && data.getApi_data().getAqi() != 0.0) {
            aqiVal = data.getApi_data().getAqi();
            int aqi = (int) aqiVal;

            tvAqiVal.setText(String.valueOf(aqi));
            tvAqiVal.setTextColor(Color.BLACK);
            indicatorBar.post(() -> {
                float barWidth = indicatorBar.getWidth();
                float maxAqi = 600f;
                float clampedAqi = Math.min(aqi, maxAqi);
                float position = (clampedAqi / maxAqi) * barWidth - aqiValIndicatorPin.getWidth() / 2f;
                position = Math.max(0, Math.min(position, barWidth - aqiValIndicatorPin.getWidth()));

                ObjectAnimator animator = ObjectAnimator.ofFloat(aqiValIndicatorPin, "translationX", position);

                indicatorBar.getViewTreeObserver().addOnDrawListener(() -> {
                    Rect scrollBounds = new Rect();
                    indicatorBar.getHitRect(scrollBounds);
                    if (!isAqiAnimated && indicatorBar.getLocalVisibleRect(scrollBounds)) {
                        animator
                                .setDuration(1500)
                                .start();
                        isAqiAnimated = true;
                    }
                });
            });
        }
        if (aqiVal != 0.0) {
            tvAqiStatVal.setText(String.format("%.1f", aqiVal));
            int aqi = (int) Math.floor(aqiVal);
            String statTxt = "";
            if (aqi <= 100) {
                statTxt = "Best Air Quality";
                tvAqiStatText.setText(statTxt);
                aqiStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00E400")));
            } else if (aqi <= 250) {
                statTxt = "Reliable Air Quality";
                tvAqiStatText.setText(statTxt);
                aqiStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFF00")));
            } else if (aqi <= 420) {
                statTxt = "Average Air Quality";
                tvAqiStatText.setText(statTxt);
                aqiStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF7E00")));
            } else if (aqi <= 500) {
                statTxt = "Poor Air Quality";
                tvAqiStatText.setText(statTxt);
                aqiStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            } else {
                statTxt = "Very Poor Air Quality";
                tvAqiStatText.setText(statTxt);
                aqiStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#99004C")));
            }
        }
    }

    public void showSkeleton() {
        skeleton.startShimmer();
        skeleton.setVisibility(View.VISIBLE);
        reportContents.setVisibility(View.GONE);
    }
    public void hideSkeleton() {
        skeleton.stopShimmer();
        skeleton.setVisibility(View.GONE);
        reportContents.setVisibility(View.VISIBLE);
    }

    private void getDetailedReport() {
        WeatherAPIService api = APIClient.getServerClient().create(WeatherAPIService.class);
        showSkeleton();
        api.getAlertReportDetails(data).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<AlertReportDetailsResponse> call, Response<AlertReportDetailsResponse> response) {
                hideSkeleton();
                if (response.isSuccessful() && response.body() != null) {
                    AlertReportDetailsResponse res = response.body();
                    if (res.isSuccess()) {
                        report = res.getReport();
                        Gson gson = new Gson();
                        String json = gson.toJson(report);
                        prefs.edit()
                                .putString("alert_report", json)
                                .putLong("alert_generated_at", System.currentTimeMillis())
                                .apply();
                        Log.d("REPORT_JSON", json);
                        initAlertDetailsCard();
                    }
                } else {
                    try {
                        String error = response.errorBody().toString();
                        JSONObject obj = new JSONObject(error);
                        String message = obj.getString("message");
                        Toaster.error(PrecautionsAndAlertDetailsActivity.this, message);
                        Log.e("ERROR", message);
                    } catch (Exception e) {
                        Toaster.error(PrecautionsAndAlertDetailsActivity.this, "Unknown Error");
                        Log.e("ERROR_FETCHING_DATA", e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<AlertReportDetailsResponse> call, Throwable t) {
                hideSkeleton();
                Toaster.error(PrecautionsAndAlertDetailsActivity.this, "Network Error: " + t.getMessage());
                Log.e("Network Error", t.getMessage());
            }
        });
    }
}