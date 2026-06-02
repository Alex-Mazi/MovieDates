package com.example.moviedates.network;

import com.example.moviedates.network.model.CheckEmailRequest;
import com.example.moviedates.network.model.CheckEmailResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/check-email")
    Call<CheckEmailResponse> checkEmail(@Body CheckEmailRequest request);

}