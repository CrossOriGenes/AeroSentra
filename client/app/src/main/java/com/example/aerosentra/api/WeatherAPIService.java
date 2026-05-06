package com.example.aerosentra.api;

import com.example.aerosentra.models.requests.GetWeatherDataRequest;
import com.example.aerosentra.models.requests.PushMessageRequest;
import com.example.aerosentra.models.response.AlertReportDetailsResponse;
import com.example.aerosentra.models.response.BasicResponse;
import com.example.aerosentra.models.response.NearbyPlacesResponse;
import com.example.aerosentra.models.response.WeatherDataResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WeatherAPIService {

    // manual prediction request
    @POST("weather/predict/v2")
    Call<WeatherDataResponse> getWeatherData(
            @Body GetWeatherDataRequest request
    );

    // get nearby places in map
    @GET("weather/map_nearby_places")
    Call<NearbyPlacesResponse> getNearbyPlaces(
            @Query("lat") double lat,
            @Query("lng") double lng
    );

    // notification alert according to weather
    @POST("weather/weather_alert_notification")
    Call<BasicResponse> sendNotification(
            @Body PushMessageRequest request
    );

    // alert report details
    @POST("weather/alert_report_details")
    Call<AlertReportDetailsResponse> getAlertReportDetails(
            @Body WeatherDataResponse.Data data
    );
}
