package com.example.aerosentra.models;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aerosentra.R;
import com.example.aerosentra.models.response.WeatherDataResponse.HourlyForecast;

import java.time.LocalTime;
import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>{

    Context context;
    List<HourlyForecast> hourlyForecastList;

    public HourlyForecastAdapter(Context context, List<HourlyForecast> hourlyForecastList) {
        this.context = context;
        this.hourlyForecastList = hourlyForecastList;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hour_forecast_item, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        HourlyForecast item = hourlyForecastList.get(position);

        String time = item.getTime();
        String timeOnly = time != null && time.contains(" ") ? time.split(" ")[1] : time;
        int hour = 0;
        if (timeOnly != null && timeOnly.contains(":")) {
            String[] parts = timeOnly.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        String ampm = (hour < 12) ? "am" : "pm";
        int displayHour = hour % 12;
        displayHour = (displayHour == 0) ? 12 : displayHour;
        String timeTxt = displayHour + " " + ampm;
        holder.time.setText(timeTxt);

        String iconUrl = "https:" + item.getIcon();
        Glide.with(context).load(iconUrl).into(holder.icon);
        String rainChanceTxt = item.getChance_of_rain() + "%";
        String tempTxt = Math.round(item.getTemp()) + "°";
        holder.precipitation.setText(rainChanceTxt);
        holder.temp.setText(tempTxt);

        int now = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) now = LocalTime.now().getHour();
        if (hour == now) {
            holder.itemView.setBackgroundResource(R.drawable.hourly_forecast_item_bg_current);
            timeTxt = "Now";
            holder.time.setText(timeTxt);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.hourly_forecast_item_bg);
            holder.time.setText(timeTxt);
        }
    }

    @Override
    public int getItemCount() {
        return hourlyForecastList.size();
    }

    public static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView time, precipitation, temp;
        ImageView icon;

        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.forecastTime);
            precipitation = itemView.findViewById(R.id.forecastChanceOfPrecip);
            temp = itemView.findViewById(R.id.forecastTemp);
            icon = itemView.findViewById(R.id.forecastIcon);
        }
    }
}
