package com.example.aerosentra.api;

import com.example.aerosentra.models.requests.GoogleLoginRequest;
import com.example.aerosentra.models.requests.SignupRequest;
import com.example.aerosentra.models.response.BasicResponse;
import com.example.aerosentra.models.response.UpdateProfileResponse;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserAPIService {

    // User Signup
    @POST("user/register_user")
    Call<BasicResponse> registerUser(
            @Body SignupRequest payload
    );

    // User Login
    @GET("user/login")
    Call<BasicResponse> loginUser(
            @Query("device_id") String deviceId,
            @Query("fcm_token") String fcmToken
    );


    // User update
    @Multipart
    @PUT("user/update_profile/{deviceId}")
    Call<UpdateProfileResponse> updateProfile(
            @Path("deviceId") String deviceId,
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part MultipartBody.Part profile
    );

    //  Delete account
    @DELETE("user/delete_account/{deviceId}")
    Call<BasicResponse> deleteAccount(
            @Path("deviceId") String deviceId
    );

}
