package com.example.aerosentra.utils;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.aerosentra.R;
import com.example.aerosentra.models.TriggerResponse;
import com.google.gson.Gson;

public class DashboardFragment extends Fragment {

    TextView tvRegionName, tvTemp, tvMaxTemp, tvMinTemp, tvFeelsLike, tvPrecipitationChance, tvCurrWeatherType, tvWindSpeed, tvPressure, tvHumidity, tvWindDir, tvAqiStat;
    ImageView currentWeatherIcon, windDirIcon, aqiStatIcon;
    TriggerResponse.Data data;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        String json = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).getString("weather_data", null);
        if (json != null) {
            Gson gson = new Gson();
            data = gson.fromJson(json, TriggerResponse.Data.class);
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
        tvAqiStat = view.findViewById(R.id.aqi_val);
        aqiStatIcon = view.findViewById(R.id.aqi_icon);

        String region = data.getApi_data().getCity_name();
        String temperature = Math.round(data.getApi_data().getTemp()) + "°";
        String temperatureMax = Math.round(data.getApi_data().getMax_temp()) + "°";
        String temperatureMin = Math.round(data.getApi_data().getMin_temp()) + "°";
        String temperatureFeelsLike = Math.round(data.getApi_data().getFeels_like()) + "°";
        String precipitation = Math.round(data.getApi_data().getPrecipitation()) + "%";
        String weatherType = data.getApi_data().getCondition().getText();
        String weatherIconUrl = "https:" + data.getApi_data().getCondition().getIcon();
        String windSpeed = Math.round(data.getApi_data().getWind()) + " Km/h";
        String pressure = Math.round(data.getApi_data().getPressure()) + " MB";
        String humidity = Math.round(data.getApi_data().getHumidity()) + " %";
        String windDirection = data.getApi_data().getWind_dir();
        String aqiStatus = data.getMl_data().getStatus();

        tvRegionName.setText(region);
        tvTemp.setText(temperature);
        tvMaxTemp.setText(temperatureMax);
        tvMinTemp.setText(temperatureMin);
        tvFeelsLike.setText(temperatureFeelsLike);
        tvPrecipitationChance.setText(precipitation);
        tvCurrWeatherType.setText(weatherType);
        Glide.with(getContext()).load(weatherIconUrl).into(currentWeatherIcon);
        currentWeatherIcon.setContentDescription(weatherType);
        tvWindSpeed.setText(windSpeed);
        tvPressure.setText(pressure);
        tvHumidity.setText(humidity);
        tvWindDir.setText(windDirection);
        rotateWindDirectionArrow(windDirection);
        setAqiStatus(aqiStatus);



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
    private void setAqiStatus(String status) {
        String val = "";
        switch (status) {
            case "GOOD":
                val = "Good";
                tvAqiStat.setText(val);
                tvAqiStat.setTextSize(20);
                aqiStatIcon.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                break;
            case "AVG":
            case "MODERATE":
                val = "Avg";
                tvAqiStat.setText(val);
                tvAqiStat.setTextSize(25);
                aqiStatIcon.setImageTintList(ColorStateList.valueOf(Color.YELLOW));
                break;
            case "POOR":
                val = "Poor";
                tvAqiStat.setText(val);
                tvAqiStat.setTextSize(25);
                aqiStatIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F27D0A")));
                break;
            case "VERY POOR":
                val = "V-Poor";
                tvAqiStat.setText(val);
                tvAqiStat.setTextSize(18);
                aqiStatIcon.setImageTintList(ColorStateList.valueOf(Color.RED));
                break;
            default:
                val = "";
                tvAqiStat.setText(val);
                aqiStatIcon.setImageTintList(null);
                break;
        }
    }
}
