package com.example.aerosentra.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static Retrofit serverRetrofit;
    private static Retrofit mdnsRetrofit;

    private static final String SERVER_API_BASE_URL = "http://192.168.0.103:8000/";
    private static final String ROVER_BASE_URL = "http://10.128.214.164/";

    // Normal backend server
    public static Retrofit getServerClient() {
        if (serverRetrofit == null) {
            serverRetrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return serverRetrofit;
    }

    // Rover mDNS
    public static Retrofit getMDNSClient() {
        if (mdnsRetrofit == null) {
            mdnsRetrofit = new Retrofit.Builder()
                    .baseUrl(ROVER_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mdnsRetrofit;
    }
}
