package com.example.aerosentra.utils;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.aerosentra.DashboardActivity;
import com.example.aerosentra.R;
import com.example.aerosentra.models.HourlyForecastAdapter;
import com.example.aerosentra.models.response.WeatherDataResponse;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;
import com.github.anastr.speedviewlib.components.indicators.Indicator;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    TextView tvRegionName, tvTemp, tvMaxTemp, tvMinTemp, tvFeelsLike, tvPrecipitationChance, tvCurrWeatherType, tvWindSpeed, tvPressure, tvHumidity, tvWindDir, tvVisibility, tvUvHeadingLabel;
    ImageView currentWeatherIcon, windDirIcon, weatherImageView, menuBtn;
    RecyclerView hourlyForecastBriefList;
    FrameLayout uvAlertMessageContainer;
    SpeedView uvGauge;

    WeatherDataResponse.Data data;
    List<WeatherDataResponse.HourlyForecast> hourly;
    boolean isUvAnimated = false, isDay;
    private JSONArray weatherConditionImagesArray;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        if (weatherConditionImagesArray == null) loadWeatherImagesConfig(requireContext());

        String json = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).getString("weather_data", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            data = gson.fromJson(json, WeatherDataResponse.Data.class);
        }


        tvRegionName = view.findViewById(R.id.regionName);
        tvTemp = view.findViewById(R.id.temperature_value);
        tvMaxTemp = view.findViewById(R.id.maxTemp);
        tvMinTemp = view.findViewById(R.id.minTemp);
        tvFeelsLike = view.findViewById(R.id.feelsLike);
        tvPrecipitationChance = view.findViewById(R.id.precipPercent);
        tvCurrWeatherType = view.findViewById(R.id.weather_now_description);
        currentWeatherIcon = view.findViewById(R.id.weather_now_icon);
        tvWindSpeed = view.findViewById(R.id.wind_speed);
        tvPressure = view.findViewById(R.id.pressure);
        tvHumidity = view.findViewById(R.id.humidity);
        tvWindDir = view.findViewById(R.id.wind_direction);
        windDirIcon = view.findViewById(R.id.wind_direction_icon);
        tvVisibility = view.findViewById(R.id.visibility_val);
        weatherImageView = view.findViewById(R.id.weather_image);

        if (data != null && data.getApi_data() != null) {
            hourly = data.getApi_data().getHourly_forecast();
            int precipitationVal1 = (int) data.getApi_data().getPrecipitation();
            int precipitationVal2 = 0;
            if (hourly != null) {
                for (WeatherDataResponse.HourlyForecast hf : hourly) {
                    if (hf.getChance_of_rain() > precipitationVal2)
                        precipitationVal2 = hf.getChance_of_rain();
                }
            }
            int precipitationVal = Math.max(precipitationVal1, precipitationVal2);

            String region = data.getApi_data().getCity_name();
            String temperature = Math.round(data.getApi_data().getTemp()) + "°";
            String temperatureMax = Math.round(data.getApi_data().getMax_temp()) + "°";
            String temperatureMin = Math.round(data.getApi_data().getMin_temp()) + "°";
            String temperatureFeelsLike = Math.round(data.getApi_data().getFeels_like()) + "°";
            String precipitation = precipitationVal + "%";
            String weatherType = data.getApi_data().getCondition().getText();
            String weatherIconUrl = "https:" + data.getApi_data().getCondition().getIcon();
            String windSpeed = Math.round(data.getApi_data().getWind()) + " Km/h";
            String pressure = Math.round(data.getApi_data().getPressure()) + " MB";
            String humidity = Math.round(data.getApi_data().getHumidity()) + " %";
            String windDirection = data.getApi_data().getWind_dir();
            String visibility = String.format("%.1f%s", data.getApi_data().getVisibility(), " Km");
            int code = data.getApi_data().getCondition().getCode();
            isDay = data.getApi_data().getIs_day();
            String imageUrl = "https:" + getImageUrl(code, isDay);

            tvRegionName.setText(region);
            tvTemp.setText(temperature);
            tvMaxTemp.setText(temperatureMax);
            tvMinTemp.setText(temperatureMin);
            tvFeelsLike.setText(temperatureFeelsLike);
            tvPrecipitationChance.setText(precipitation);
            tvCurrWeatherType.setText(weatherType);
            Glide.with(getContext()).load(weatherIconUrl).into(currentWeatherIcon);
            Glide.with(getContext())
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(weatherImageView);
            currentWeatherIcon.setContentDescription(weatherType);
            tvWindSpeed.setText(windSpeed);
            tvPressure.setText(pressure);
            tvHumidity.setText(humidity);
            tvWindDir.setText(windDirection);
            rotateWindDirectionArrow(windDirection);
            tvVisibility.setText(visibility);
        }

        hourlyForecastBriefList = view.findViewById(R.id.hourlyForecastBriefList);
        hourlyForecastBriefList.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );
        if (data != null && data.getApi_data() != null && hourly != null) {
            int currHr = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) currHr = LocalTime.now().getHour();
            List<WeatherDataResponse.HourlyForecast> next3hrs = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                int index = (currHr + i) % hourly.size();
                next3hrs.add(hourly.get(index));
            }
            HourlyForecastAdapter hourlyForecastAdapter = new HourlyForecastAdapter(getContext(), next3hrs);
            hourlyForecastBriefList.setAdapter(hourlyForecastAdapter);
            hourlyForecastBriefList.setHasFixedSize(true);
        }


        // UV GAUGE
        uvGauge = view.findViewById(R.id.uvSpeedView);
        tvUvHeadingLabel = view.findViewById(R.id.uv_label_heading);
        uvAlertMessageContainer = view.findViewById(R.id.uvAlertContainer);
        Typeface semiBoldTf = ResourcesCompat.getFont(getContext(), R.font.jost_semibold);
        if (semiBoldTf != null) {
            uvGauge.setSpeedTextTypeface(semiBoldTf);
        }
        tvUvHeadingLabel.setTextColor(Color.WHITE);
        loadUvRadial(view);


        // Menu button
        menuBtn = view.findViewById(R.id.moreItemsMenu1);
        menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.back) {
                    startActivity(new Intent(getContext(), DashboardActivity.class));
                    getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    getActivity().finish();
                    return true;
                } else return false;
            });
            popup.show();
        });


        return view;
    }

    private void rotateWindDirectionArrow(String direction) {
        switch (direction) {
            case "N":
                windDirIcon.setRotation(0);
                break;
            case "S":
                windDirIcon.setRotation(180);
                break;
            case "E":
                windDirIcon.setRotation(90);
                break;
            case "W":
                windDirIcon.setRotation(-90);
                break;
            case "NE":
                windDirIcon.setRotation(45);
                break;
            case "NW":
                windDirIcon.setRotation(-45);
                break;
            case "SE":
                windDirIcon.setRotation(135);
                break;
            case "SW":
                windDirIcon.setRotation(-135);
                break;
            case "SSW":
                windDirIcon.setRotation(-158);
                break;
            case "NNW":
                windDirIcon.setRotation(-22);
                break;
            default:
                windDirIcon.setVisibility(INVISIBLE);
                break;
        }
    }
    private void loadUvRadial(View view) {
        TextView tvUVVal = view.findViewById(R.id.uvVal);
        TextView tvUVLvlStat = view.findViewById(R.id.uvLvlStat);
        View uvLvlStatDot = view.findViewById(R.id.uvLvlStatDot);
        LayoutInflater alertInflater = LayoutInflater.from(getContext());
        View alertView = alertInflater.inflate(R.layout.alert_box, null);
        TextView alertMsg = alertView.findViewById(R.id.alertMsg);

        double uvValue;
        if (data != null && data.getApi_data() != null) {
            double maxUv = 0.0;
            for (WeatherDataResponse.HourlyForecast hf : hourly) {
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
        uvGauge.setSpeedTextColor(Color.parseColor("#CCCCCC"));
        uvGauge.getIndicator().setColor(Color.WHITE);
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
            if (isDay) {
                uvAlertMessageContainer.setVisibility(View.VISIBLE);
                alertMsg.setText(R.string.uvAlert_msg);
                uvAlertMessageContainer.addView(alertView);
            } else uvAlertMessageContainer.setVisibility(View.GONE);
        } else if (Math.round(uvValue) <= 10) {
            uvStat = "Very High";
            tvUVLvlStat.setTextSize(14);
            uvLvlStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CC0000")));
            if (isDay) {
                uvAlertMessageContainer.setVisibility(View.VISIBLE);
                alertMsg.setText(R.string.uvAlert_msg);
                uvAlertMessageContainer.addView(alertView);
            } else uvAlertMessageContainer.setVisibility(View.GONE);
        } else {
            uvStat = "Extreme";
            uvLvlStatDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#660033")));
            if (isDay) {
                uvAlertMessageContainer.setVisibility(View.VISIBLE);
                alertMsg.setText(R.string.uvAlert_msg);
                uvAlertMessageContainer.addView(alertView);
            } else uvAlertMessageContainer.setVisibility(View.GONE);
        }
        tvUVLvlStat.setText(uvStat);
        tvUVVal.setText(String.format("%.1f", uvValue));
    }

    private void loadWeatherImagesConfig(Context ctx) {
        try {
            InputStream is = ctx.getAssets().open("weather_conditions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            weatherConditionImagesArray = new JSONArray(json);
        } catch (Exception e) {
            Log.e("loadWeatherImagesConfig", e.getMessage());
        }
    }
    private String getImageUrl(int code, boolean isDay) {
        try {
            for (int i = 0; i < weatherConditionImagesArray.length(); i++) {
                JSONObject obj = weatherConditionImagesArray.getJSONObject(i);
                JSONArray codes = obj.getJSONArray("codes");
                for (int j = 0; j < codes.length(); j++) {
                    if (codes.getInt(j) == code) {
                        return isDay ? obj.getString("image_day") : obj.getString("image_night");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("JSON Exception", e.getMessage());
        }

        return isDay ?
                "//res.cloudinary.com/dtfoedy3u/image/upload/v1777042321/clear_day_lyd4sn.png":
                "//res.cloudinary.com/dtfoedy3u/image/upload/v1777042321/clear_night_xczrkn.png";
    }
}
