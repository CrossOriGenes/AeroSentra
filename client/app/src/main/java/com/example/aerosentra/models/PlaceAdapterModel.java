package com.example.aerosentra.models;

import java.util.List;

public class PlaceAdapterModel {

    String icon;
    String type;
    String city;
    String region;
    double lat;
    double lng;
    double temperature;
    double uv;
    double humidity;
    double pressure;
    List<String> images;

    public PlaceAdapterModel(String icon, String type, String city, String region, double lat, double lng, double temperature, double uv, double humidity, double pressure, List<String> images) {
        this.icon = icon;
        this.type = type;
        this.city = city;
        this.region = region;
        this.lat = lat;
        this.lng = lng;
        this.temperature = temperature;
        this.uv = uv;
        this.humidity = humidity;
        this.pressure = pressure;
        this.images = images;
    }

    public String getIcon() { return icon; }
    public String getType() { return type; }
    public String getCity() { return city; }
    public String getRegion() { return region; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getTemperature() { return temperature; }
    public double getUv() { return uv; }
    public double getHumidity() { return humidity; }
    public double getPressure() { return pressure; }
    public List<String> getImages() { return images; }

    public void setIcon(String icon) { this.icon = icon; }
    public void setType(String type) { this.type = type; }
    public void setCity(String city) { this.city = city; }
    public void setRegion(String region) { this.region = region; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setUv(double uv) { this.uv = uv; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public void setPressure(double pressure) { this.pressure = pressure; }
    public void setImages(List<String> images) { this.images = images; }

}
