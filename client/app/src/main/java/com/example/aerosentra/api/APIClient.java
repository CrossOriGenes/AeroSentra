package com.example.aerosentra.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static Retrofit serverRetrofit;
    private static Retrofit mdnsRetrofit;

    private static final String SERVER_API_BASE_URL = "https://aerosentra.onrender.com/api/";
//    private static final String SERVER_API_BASE_URL = "http://10.74.113.229:8000/api/";
    private static final String ROVER_BASE_URL = "http://10.74.113.164/";

    private static OkHttpClient getHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        try {
                            return chain.proceed(chain.request());
                        } catch (IllegalStateException e) {
                            // Catch malformed response codes (like -11) to prevent crash
                            if (e.getMessage() != null && e.getMessage().contains("code < 0")) {
                                throw new IOException("Malformed HTTP response from server: " + e.getMessage(), e);
                            }
                            throw e;
                        }
                    }
                })
                .build();
    }


    // Normal backend server
    public static Retrofit getServerClient() {
        if (serverRetrofit == null) {
            serverRetrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_API_BASE_URL)
                    .client(getHttpClient())
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
                    .client(getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mdnsRetrofit;
    }
}
