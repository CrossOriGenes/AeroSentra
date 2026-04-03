package com.example.aerosentra.models;

public class RoverStatus {
    private final String device;
    private final String status;
    private final String address;

    public RoverStatus(String device, String status, String address) {
        this.device = device;
        this.status = status;
        this.address = address;
    }

    public String getDevice() {
        return device;
    }

    public String getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }
}
