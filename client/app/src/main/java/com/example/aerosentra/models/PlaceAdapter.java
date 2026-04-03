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

import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    Context context;
    ArrayList<PlaceAdapterModel> list;

    public PlaceAdapter(Context context, ArrayList<PlaceAdapterModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.place_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceAdapterModel model = list.get(position);
        String cityName = model.getCity();
        if (cityName.length() > 15) cityName = cityName.substring(0, 15) + "...";
        holder.city.setText(cityName);
        holder.weatherType.setText(model.getType());
        String temperature = Math.round(model.getTemperature()) + "°";
        holder.temperature.setText(temperature);
        String iconUrl = "https:" + model.getIcon();
        Glide.with(context).load(iconUrl).into(holder.icon);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView city, temperature, weatherType;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.placeName);
            weatherType = itemView.findViewById(R.id.placeWeatherType);
            temperature = itemView.findViewById(R.id.placeTemp);
            icon = itemView.findViewById(R.id.placeIcon);
        }
    }
}
