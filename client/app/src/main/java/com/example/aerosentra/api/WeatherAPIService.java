package com.example.aerosentra.api;

import com.example.aerosentra.models.requests.GetWeatherDataRequest;
import com.example.aerosentra.models.response.NearbyPlacesResponse;
import com.example.aerosentra.models.response.TriggerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WeatherAPIService {

    // manual prediction request
    @POST("predict/v2")
    Call<TriggerResponse> getWeatherData(
            @Body GetWeatherDataRequest request
    );

    // get nearby places in map
    @GET("map_nearby_places")
    Call<NearbyPlacesResponse> getNearbyPlaces(
            @Query("lat") double lat,
            @Query("lng") double lng
    );

}
