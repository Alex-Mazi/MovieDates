package com.example.moviedates.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;
import com.example.moviedates.BuildConfig;

public class ApiClient {

    private static final String BASE_URL = BuildConfig.BASE_URL;

    private static Retrofit instance = null;

    private ApiClient() {}

    public static synchronized Retrofit getInstance(Context context) {
        if (instance == null) {instance = buildRetrofit(context.getApplicationContext());}
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    private static Retrofit buildRetrofit(Context appContext) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    SharedPreferences prefs = appContext.getSharedPreferences("moviedates_prefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("jwt_token", null);

                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder().header("Content-Type", "application/json").header("Accept", "application/json");

                    if (token != null && !token.isEmpty()) {builder.header("Authorization", "Bearer " + token);}

                    return chain.proceed(builder.build());
                }).addInterceptor(logging).build();

        return new Retrofit.Builder().baseUrl(BASE_URL).client(httpClient).addConverterFactory(GsonConverterFactory.create()).build();
    }
}