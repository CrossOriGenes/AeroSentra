package com.example.aerosentra.api;

import com.example.aerosentra.models.TriggerResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RoverAPIService {
    @GET("trigger")
    Call<TriggerResponse> triggerRover();
}
