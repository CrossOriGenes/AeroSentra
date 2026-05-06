package com.example.aerosentra.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aerosentra.R;
import com.example.aerosentra.models.response.WeatherDataResponse.DailyForecast;

import java.util.List;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder>{

    Context context;
    List<DailyForecast> dailyForecastList;

    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


    public DailyForecastAdapter(Context context, List<DailyForecast> dailyForecastList) {
        this.context = context;
        this.dailyForecastList = dailyForecastList;
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.daily_forecast_item, parent, false);
        return new DailyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        DailyForecast item = dailyForecastList.get(position);

        String date = item.getDate();
        String dateTxt = extractDate(date);
        holder.date.setText(dateTxt);

        String iconUrl = "https:" + item.getIcon();
        Glide.with(context).load(iconUrl).into(holder.icon);

        String maxTempTxt = Math.round(item.getMax_temp()) + "°";
        holder.maxTemp.setText(maxTempTxt);

        String minTempTxt = Math.round(item.getMin_temp()) + "°";
        holder.minTemp.setText(minTempTxt);

        String descriptionTxt = item.getType();
        if (descriptionTxt.length() > 13) descriptionTxt = descriptionTxt.substring(0, 12) + "...";
        holder.description.setText(descriptionTxt);

        if (position == dailyForecastList.size() - 1) holder.bottomBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dailyForecastList.size();
    }

    public static class DailyViewHolder extends RecyclerView.ViewHolder {
        TextView date, maxTemp, minTemp, description;
        ImageView icon;
        View bottomBar;

        public DailyViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.forecastDailyDate);
            icon = itemView.findViewById(R.id.forecastDailyIcon);
            maxTemp = itemView.findViewById(R.id.forecastDailyMaxTemp);
            minTemp = itemView.findViewById(R.id.forecastDailyMinTemp);
            description = itemView.findViewById(R.id.forecastDailyDescription);
            bottomBar = itemView.findViewById(R.id.bottomBar);
        }
    }

    public static String extractDate(String date) {
        String[] splittedDate = date != null && date.contains("-") ? date.split("-") : null;
        String dateOnly = splittedDate != null ? splittedDate[2] : date;
        String monthOnly = splittedDate != null ? splittedDate[1] : date;
        String month = "";
        if (monthOnly != null) month = MONTHS[Integer.parseInt(monthOnly) - 1];
        return dateOnly + " " + month;
    }
}
