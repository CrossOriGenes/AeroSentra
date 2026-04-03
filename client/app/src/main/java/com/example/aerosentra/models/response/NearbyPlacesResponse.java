package com.example.aerosentra.models.response;

import com.example.aerosentra.models.PlaceAdapterModel;

import java.util.List;

public class NearbyPlacesResponse {
    boolean success;
    String message;
    List<PlaceAdapterModel> places;

    public List<PlaceAdapterModel> getPlaces() { return places; }
    public boolean isSuccess() { return success; }
    public String getMsg() { return message; }
}
