package com.example.aerosentra.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aerosentra.DashboardActivity;
import com.example.aerosentra.R;
import com.example.aerosentra.RoverTriggerActivity;

public class SettingsFragment extends Fragment {
    Button rePredictBtn;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String weatherData = prefs.getString("weather_data", "");

        rePredictBtn = view.findViewById(R.id.rePredictBtn);
        rePredictBtn.setOnClickListener(v -> {
            if (!weatherData.isEmpty()){
                prefs.edit().remove("weather_data").apply();
                prefs.edit().remove("lat").apply();
                prefs.edit().remove("lon").apply();
            }
            startActivity(new Intent(getContext(), DashboardActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            getActivity().finish();
        });

        return view;
    }
}
