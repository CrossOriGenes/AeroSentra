package com.example.aerosentra.models.requests;

public class GetWeatherDataRequest {
    float lat, lon;

    public GetWeatherDataRequest(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public float getLat() { return lat; }
    public float getLon() { return lon; }
}
