package com.example.moviedates.network;

import com.example.moviedates.network.model.AuthRequest;
import com.example.moviedates.network.model.AuthResponse;
import com.example.moviedates.network.model.MovieDTO;
import com.example.moviedates.network.model.SessionResponse;
import com.example.moviedates.network.model.SoloSwipeRequest;
import com.example.moviedates.network.model.VoteRequest;

import java.util.List;
import java.util.Map;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/guest")
    Call<AuthResponse> registerGuest(@Body Map<String, String> request);

    @POST("api/sessions/create")
    Call<SessionResponse> createRoom();

    @POST("api/sessions/join/{code}")
    Call<SessionResponse> joinRoom(@Path("code") String code, @Body Map<String, Long> body);

    @POST("api/sessions/start/{code}")
    Call<SessionResponse> startSession(@Path("code") String code);

    @GET("api/sessions/{code}/deck")
    Call<List<MovieDTO>> getDeck(@Path("code") String code);

    @POST("api/votes")
    Call<Map<String, Object>> submitVote(@Body VoteRequest body);

    @PUT("api/users/{id}/genres")
    Call<AuthResponse.UserPayload> updateGenres(@Path("id") long id, @Body List<Integer> genreIds);

    @GET("api/users/{id}")
    Call<AuthResponse.UserPayload> getUser(@Path("id") long id);

    @GET("api/movies/discover")
    Call<Map<String, Object>> discoverMovies(@Query(value = "genres", encoded = false) List<Integer> genres);

    @GET("api/movies/genres")
    Call<Map<String, Object>> getGenres();

    @GET("api/users/{id}/solo-deck")
    Call<Map<String, Object>> getSoloDeck(@Path("id") long userId);

    @POST("api/users/{id}/solo-swipe")
    Call<Void> postSoloSwipe(@Path("id") long userId, @Body SoloSwipeRequest body);

}