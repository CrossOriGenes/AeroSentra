package com.example.aerosentra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.RoverAPIService;
import com.example.aerosentra.models.TriggerResponse;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.Toaster;
import com.google.gson.Gson;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoverTriggerActivity extends AppCompatActivity {

    Button triggerCallBtn;
    PopupUtils loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rover_trigger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loader = new PopupUtils();
        triggerCallBtn = findViewById(R.id.trigger_call_button);


        triggerCallBtn.setOnClickListener(v -> {

            RoverAPIService roverApi = APIClient.getMDNSClient().create(RoverAPIService.class);

            loader.showLoader(this, "Getting earth data...");
            Call<TriggerResponse> call = roverApi.triggerRover();

            call.enqueue(new Callback<TriggerResponse>() {
                @Override
                public void onResponse(Call<TriggerResponse> call, Response<TriggerResponse> response) {
                    loader.dismiss();

                    if (response.isSuccessful() && response.body()!=null) {
                        TriggerResponse res = response.body();

                        if (res.isSuccess()) {
                            Toaster.success(RoverTriggerActivity.this, res.getMsg());

                            Gson gson = new Gson();
                            String json = gson.toJson(res.getData());

                            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("weather_data", json);
                            editor.apply();

                            startActivity(new Intent(RoverTriggerActivity.this, MainActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            try {
                                String error = response.errorBody().string();
                                JSONObject obj = new JSONObject(error);
                                String message = obj.getString("message");

                                Toaster.error(RoverTriggerActivity.this, message);
                            } catch (Exception e) {
                                Toaster.error(RoverTriggerActivity.this, "Unknown Error");
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<TriggerResponse> call, Throwable t) {
                    loader.dismiss();
                    Toaster.error(RoverTriggerActivity.this, "Network Error: "+t.getMessage());
                }
            });
        });
    }
}