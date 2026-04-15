package com.example.medisportuz;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoApiService {
    String BASE_URL = "https://api.open-meteo.com/v1/";

    @GET("forecast")
    Call<OpenMeteoResponse> getForecast(
            @Query("latitude")               double lat,
            @Query("longitude")              double lon,
            @Query("current")                String currentVars,
            @Query("daily")                  String dailyVars,
            @Query("timezone")               String timezone,
            @Query("forecast_days")          int forecastDays
    );
}