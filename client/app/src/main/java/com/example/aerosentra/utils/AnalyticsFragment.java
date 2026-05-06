package com.example.aerosentra.utils;

import static android.content.Context.MODE_PRIVATE;

import static com.example.aerosentra.models.DailyForecastAdapter.MONTHS;

import android.animation.ObjectAnimator;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerosentra.R;
import com.example.aerosentra.models.DailyForecastAdapter;
import com.example.aerosentra.models.HourlyForecastAdapter;
import com.example.aerosentra.models.response.WeatherDataResponse;
import com.example.aerosentra.ui.PercentageValueMarkerView;
import com.example.aerosentra.ui.TemperatureMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    TextView tvAqiVal, tvAqiStatVal, tvAqiStatText, tvAqiHeadingLabel;
    RecyclerView dailyForecastRecycler, hourlyForecastRecycler;
    LinearLayout aqiValIndicatorPin, indicatorBar;
    MaterialButtonToggleGroup temperatureChartTypeToggler, humidityChartTypeToggler;
    LineChart temperatureChart, rainfallChart, humidityChart;
    Legend legend, legend2, legend3;


    SharedPreferences prefs;
    WeatherDataResponse.Data data;
    List<WeatherDataResponse.DailyForecast> daily;
    List<WeatherDataResponse.HourlyForecast> hourly;
    boolean isTemperatureAnimated = false,
            isHumidityAnimated = false,
            isRainfallAnimated = false,
            isAqiAnimated = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        prefs = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String json = prefs.getString("weather_data", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            data = gson.fromJson(json, WeatherDataResponse.Data.class);
        }

        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.jost_medium);



        // DAILY & HOURLY FORECAST
        dailyForecastRecycler = view.findViewById(R.id.dailyForecastFullList);
        dailyForecastRecycler.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                )
        );
        if (data != null && data.getApi_data() != null) {
            daily = data.getApi_data().getDaily_forecast();
            DailyForecastAdapter dailyForecastAdapter = new DailyForecastAdapter(getContext(), daily);
            dailyForecastRecycler.setAdapter(dailyForecastAdapter);
            hourlyForecastRecycler = view.findViewById(R.id.hourlyForecastFullList);
            hourlyForecastRecycler.setLayoutManager(
                    new LinearLayoutManager(
                            getContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                    )
            );
            hourly = data.getApi_data().getHourly_forecast();
            HourlyForecastAdapter hourlyForecastAdapter = new HourlyForecastAdapter(getContext(), hourly);
            hourlyForecastRecycler.setAdapter(hourlyForecastAdapter);
        }



        // TEMPERATURE CHART
        temperatureChart = view.findViewById(R.id.lineChartForTemperature);
        temperatureChart.getDescription().setEnabled(false);
        legend = temperatureChart.getLegend();
        legend.setEnabled(false);
        temperatureChart.getAxisRight().setEnabled(false);
        temperatureChart.getXAxis().setDrawGridLines(false);
        temperatureChart.getAxisLeft().setDrawGridLines(false);
        temperatureChart.getXAxis().setTypeface(tf);
        temperatureChart.getAxisLeft().setTypeface(tf);
        temperatureChart.getXAxis().setTextColor(Color.WHITE);
        temperatureChart.getAxisLeft().setTextColor(Color.WHITE);
        TemperatureMarkerView marker = new TemperatureMarkerView(getContext(), R.layout.chart_marker);
        marker.setChartView(temperatureChart);
        temperatureChart.setMarker(marker);

        temperatureChartTypeToggler = view.findViewById(R.id.minitabToggler1);
        loadHourlyChart();
        temperatureChartTypeToggler.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnHourly){
                isTemperatureAnimated = false;
                legend.setEnabled(false);
                loadHourlyChart();
            } else if (checkedId == R.id.btnDaily) {
                isTemperatureAnimated = false;
                setLegend(tf);
                loadDailyChart();
            }
        });



        //  HUMIDITY CHART
        humidityChart = view.findViewById(R.id.lineChartForHumidity);
        humidityChart.getDescription().setEnabled(false);
        legend2 = humidityChart.getLegend();
        legend2.setEnabled(false);
        humidityChart.getAxisRight().setEnabled(false);
        humidityChart.getXAxis().setDrawGridLines(false);
        humidityChart.getAxisLeft().setDrawGridLines(false);
        humidityChart.getXAxis().setTypeface(tf);
        humidityChart.getAxisLeft().setTypeface(tf);
        humidityChart.getXAxis().setTextColor(Color.WHITE);
        humidityChart.getAxisLeft().setTextColor(Color.WHITE);
        PercentageValueMarkerView marker2 = new PercentageValueMarkerView(getContext(), R.layout.chart_marker);
        marker2.setChartView(humidityChart);
        humidityChart.setMarker(marker2);

        humidityChartTypeToggler = view.findViewById(R.id.minitabToggler2);
        loadHourlyHumidityChart();
        humidityChartTypeToggler.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
           if (!isChecked) return;
           if (checkedId == R.id.btnHourly2){
               isHumidityAnimated = false;
               loadHourlyHumidityChart();
           } else if (checkedId == R.id.btnDaily2) {
               isHumidityAnimated = false;
               loadDailyHumidityChart();
           }
        });



        // RAINFALL CHART
        rainfallChart = view.findViewById(R.id.areaChartForRainfall);
        rainfallChart.getDescription().setEnabled(false);
        legend3 = rainfallChart.getLegend();
        legend3.setEnabled(false);
        rainfallChart.getAxisRight().setEnabled(false);
        rainfallChart.getXAxis().setDrawGridLines(false);
        rainfallChart.getAxisLeft().setDrawGridLines(false);
        rainfallChart.getXAxis().setTypeface(tf);
        rainfallChart.getAxisLeft().setTypeface(tf);
        rainfallChart.getXAxis().setTextColor(Color.WHITE);
        rainfallChart.getAxisLeft().setTextColor(Color.WHITE);
        PercentageValueMarkerView marker3 = new PercentageValueMarkerView(getContext(), R.layout.chart_marker);
        marker3.setChartView(rainfallChart);
        rainfallChart.setMarker(marker3);

        loadRainfallChart();


        // AQI INDICATOR BAR
        aqiValIndicatorPin = view.findViewById(R.id.aqiValIndicatorPin);
        indicatorBar = view.findViewById(R.id.indicatorBar);
        tvAqiHeadingLabel = view.findViewById(R.id.aqi_label_heading);
        tvAqiHeadingLabel.setTextColor(Color.WHITE);
        tvAqiVal = aqiValIndicatorPin.findViewById(R.id.aqiValue);
        tvAqiStatVal = view.findViewById(R.id.aqiStatVal);
        tvAqiStatText = view.findViewById(R.id.aqiStatText);
        View aqiStatDot = view.findViewById(R.id.aqiStatDot);

        double aqiVal = 0.0;
        if (data != null && data.getApi_data() != null && data.getApi_data().getAqi() != 0.0) {
            aqiVal = data.getApi_data().getAqi();
            int aqi = (int) aqiVal;

            tvAqiVal.setText(String.valueOf(aqi));
            tvAqiVal.setTextColor(Color.WHITE);
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




        return view;
    }

    private void setLegend(Typeface tf) {
        legend.setEnabled(true);
        legend.setTypeface(tf);
        legend.setTextColor(Color.WHITE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
    }
    private void loadHourlyChart() {
        if (hourly == null) return;
        temperatureChart.highlightValues(null);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < hourly.size(); i++) {
            double temp = hourly.get(i).getTemp();
            entries.add(new Entry(i, (float) temp));
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < hourly.size(); i++) {
            String time = hourly.get(i).getTime();
            int hour = 0;
            if (time != null && time.contains(":")) {
                String[] parts = time.split(":");
                try {
                    hour = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    Log.e("AnalyticsFragment", "Error parsing hour: " + e.getMessage());
                }
            }
            String ampm = (hour < 12) ? "am" : "pm";
            int displayHour = hour % 12;
            displayHour = (displayHour == 0) ? 12 : displayHour;
            String timeTxt = displayHour + " " + ampm;
            labels.add(timeTxt);
        }
        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        dataSet.setColor(Color.parseColor("#ff9800"));
        dataSet.setCircleColor(Color.parseColor("#ff9800"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);
        temperatureChart.setData(new LineData(dataSet));
        temperatureChart.invalidate();
        temperatureChart.setVisibleXRangeMaximum(8);
        temperatureChart.getViewTreeObserver().addOnDrawListener(() -> {
            Rect scrollBounds = new Rect();
            temperatureChart.getHitRect(scrollBounds);
            if (!isTemperatureAnimated && temperatureChart.getLocalVisibleRect(scrollBounds)) {
                temperatureChart.animateXY(900, 1200);
                isTemperatureAnimated = true;
            }
        });
    }
    private void loadDailyChart() {
        if (daily == null) return;
        temperatureChart.highlightValues(null);
        List<Entry> maxEntries = new ArrayList<>();
        List<Entry> minEntries = new ArrayList<>();
        for (int i = 0; i < daily.size(); i++) {
            double maxTemp = daily.get(i).getMax_temp();
            maxEntries.add(new Entry(i, (float) maxTemp));
            double minTemp = daily.get(i).getMin_temp();
            minEntries.add(new Entry(i, (float) minTemp));
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < daily.size(); i++) {
            String date = daily.get(i).getDate();
            String[] splittedDate = date != null && date.contains("-") ? date.split("-") : null;
            String dateOnly = splittedDate != null ? splittedDate[2] : date;
            String monthOnly = splittedDate != null ? splittedDate[1] : date;
            String month = "";
            if (monthOnly != null) month = MONTHS[Integer.parseInt(monthOnly) - 1];
            String timeTxt = dateOnly + " " + month;
            labels.add(timeTxt);
        }
        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        //  MAXIMUM TEMPERATURE LINE
        LineDataSet maxDataSet = new LineDataSet(maxEntries, "Max");
        maxDataSet.setColor(Color.parseColor("#ff0066"));
        maxDataSet.setCircleColor(Color.parseColor("#ff0066"));
        maxDataSet.setLineWidth(2f);
        maxDataSet.setCircleRadius(5f);
        maxDataSet.setDrawValues(false);
        maxDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //  MINIMUM TEMPERATURE LINE
        LineDataSet minDataSet = new LineDataSet(minEntries, "Min");
        minDataSet.setColor(Color.parseColor("#00ff99"));
        minDataSet.setCircleColor(Color.parseColor("#00ff99"));
        minDataSet.setLineWidth(2f);
        minDataSet.setCircleRadius(5f);
        minDataSet.setDrawValues(false);
        minDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        temperatureChart.setData(new LineData(maxDataSet, minDataSet));
        temperatureChart.invalidate();
        temperatureChart.setVisibleXRangeMaximum(4);
        temperatureChart.getViewTreeObserver().addOnDrawListener(() -> {
            Rect scrollBounds = new Rect();
            temperatureChart.getHitRect(scrollBounds);
            if (!isTemperatureAnimated && temperatureChart.getLocalVisibleRect(scrollBounds)) {
                temperatureChart.animateXY(900, 1200);
                isTemperatureAnimated = true;
            }
        });
    }
    private void loadHourlyHumidityChart() {
        if (hourly == null) return;
        humidityChart.highlightValues(null);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < hourly.size(); i++) {
            double humidity = hourly.get(i).getHumidity();
            entries.add(new Entry(i, (float) humidity));
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < hourly.size(); i++) {
            String time = hourly.get(i).getTime();
            int hour = 0;
            if (time != null && time.contains(":")) {
                String[] parts = time.split(":");
                try {
                    hour = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    Log.e("AnalyticsFragment", "Error parsing hour: " + e.getMessage());
                }
            }
            String ampm = (hour < 12) ? "am" : "pm";
            int displayHour = hour % 12;
            displayHour = (displayHour == 0) ? 12 : displayHour;
            String timeTxt = displayHour + " " + ampm;
            labels.add(timeTxt);
        }
        XAxis xAxis = humidityChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        LineDataSet dataSet = new LineDataSet(entries, "Humidity");
        dataSet.setColor(Color.parseColor("#00ff99"));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#00ff99"));
        dataSet.setFillAlpha(80);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        humidityChart.setData(new LineData(dataSet));
        humidityChart.invalidate();
        humidityChart.setVisibleXRangeMaximum(8);
        humidityChart.getAxisLeft().setAxisMinimum(0f);
        humidityChart.getViewTreeObserver().addOnDrawListener(() -> {
            Rect scrollBounds = new Rect();
            humidityChart.getHitRect(scrollBounds);
            if (!isHumidityAnimated && humidityChart.getLocalVisibleRect(scrollBounds)) {
                humidityChart.animateXY(900, 1200);
                isHumidityAnimated = true;
            }
        });
    }
    private void loadDailyHumidityChart() {
        if (daily == null) return;
        humidityChart.highlightValues(null);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < daily.size(); i++) {
            double humidity = daily.get(i).getHumidity();
            entries.add(new Entry(i, (float) humidity));
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < daily.size(); i++) {
            String date = daily.get(i).getDate();
            String[] splittedDate = date != null && date.contains("-") ? date.split("-") : null;
            String dateOnly = splittedDate != null ? splittedDate[2] : date;
            String monthOnly = splittedDate != null ? splittedDate[1] : date;
            String month = "";
            if (monthOnly != null) month = MONTHS[Integer.parseInt(monthOnly) - 1];
            String timeTxt = dateOnly + " " + month;
            labels.add(timeTxt);
        }
        XAxis xAxis = humidityChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        LineDataSet dataSet = new LineDataSet(entries, "Humidity");
        dataSet.setColor(Color.parseColor("#CC66FF"));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#CC66FF"));
        dataSet.setFillAlpha(80);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        humidityChart.setData(new LineData(dataSet));
        humidityChart.invalidate();
        humidityChart.setVisibleXRangeMaximum(4);
        humidityChart.getAxisLeft().setAxisMinimum(0f);
        humidityChart.getViewTreeObserver().addOnDrawListener(() -> {
            Rect scrollBounds = new Rect();
            humidityChart.getHitRect(scrollBounds);
            if (!isHumidityAnimated && humidityChart.getLocalVisibleRect(scrollBounds)) {
                humidityChart.animateXY(900, 1200);
                isHumidityAnimated = true;
            }
        });
    }
    private void loadRainfallChart() {
        if (hourly == null) return;
        rainfallChart.highlightValues(null);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < hourly.size(); i++) {
            double rain = hourly.get(i).getChance_of_rain();
            entries.add(new Entry(i, (float) rain));
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < hourly.size(); i++) {
            String time = hourly.get(i).getTime();
            int hour = 0;
            if (time != null && time.contains(":")) {
                String[] parts = time.split(":");
                try {
                    hour = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    Log.e("AnalyticsFragment", "Error parsing hour: " + e.getMessage());
                }
            }
            String ampm = (hour < 12) ? "am" : "pm";
            int displayHour = hour % 12;
            displayHour = (displayHour == 0) ? 12 : displayHour;
            String timeTxt = displayHour + " " + ampm;
            labels.add(timeTxt);
        }
        XAxis xAxis = rainfallChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        LineDataSet dataSet = new LineDataSet(entries, "Rainfall");
        dataSet.setColor(Color.parseColor("#33ccff"));
        dataSet.setCircleColor(Color.parseColor("#33ccff"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#33ccff"));
        dataSet.setFillAlpha(120);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        rainfallChart.setData(new LineData(dataSet));
        rainfallChart.invalidate();
        rainfallChart.setVisibleXRangeMaximum(8);
        rainfallChart.getAxisLeft().setAxisMinimum(0f);
        rainfallChart.getViewTreeObserver().addOnDrawListener(() -> {
            Rect scrollBounds = new Rect();
            rainfallChart.getHitRect(scrollBounds);
            if (!isRainfallAnimated && rainfallChart.getLocalVisibleRect(scrollBounds)) {
                rainfallChart.animateXY(900, 1200);
                isRainfallAnimated = true;
            }
        });
    }

}
