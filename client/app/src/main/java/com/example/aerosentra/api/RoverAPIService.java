package com.example.aerosentra.api;

import com.example.aerosentra.models.RoverStatus;
import com.example.aerosentra.models.response.TriggerResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RoverAPIService {
    @GET("check")
    Call<RoverStatus> checkStatus();

    @GET("trigger")
    Call<TriggerResponse> triggerRover();
}
