package com.example.medisportuz;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApiService {
    String BASE_URL = "https://geocoding-api.open-meteo.com/v1/";

    @GET("search")
    Call<GeocodingResponse> searchCity(
            @Query("name")     String name,
            @Query("count")    int count,
            @Query("language") String language
    );
}