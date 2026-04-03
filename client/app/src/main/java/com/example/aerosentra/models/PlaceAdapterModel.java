package com.example.aerosentra.models;

public class MapPlaceModel {
    String id;
    String icon;
    String city;
    double lat;
    double lng;
    double temperature;

    public MapPlaceModel(String id, String icon, String city, double lat, double lng, double temperature) {
        this.id = id;
        this.icon = icon;
        this.city = city;
        this.lat = lat;
        this.lng = lng;
        this.temperature = temperature;
    }

    public String getId() { return id; }
    public String getIcon() { return icon; }
    public String getCity() { return city; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getTemperature() { return temperature; }

    public void setId(String id) { this.id = id; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setCity(String city) { this.city = city; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

}
