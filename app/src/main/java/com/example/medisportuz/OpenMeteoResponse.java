package com.example.medisportuz;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenMeteoResponse {

    // Bieżąca pogoda
    @SerializedName("current")
    public Current current;

    // Prognoza dzienna
    @SerializedName("daily")
    public Daily daily;

    public static class Current {
        @SerializedName("temperature_2m")
        public float temperature;

        @SerializedName("weather_code")
        public int weatherCode;

        @SerializedName("wind_speed_10m")
        public float windSpeed;

        @SerializedName("relative_humidity_2m")
        public int humidity;
    }

    public static class Daily {
        @SerializedName("time")
        public List<String> time;             // np. ["2025-03-25", "2025-03-26", ...]

        @SerializedName("temperature_2m_max")
        public List<Float> tempMax;

        @SerializedName("temperature_2m_min")
        public List<Float> tempMin;

        @SerializedName("weather_code")
        public List<Integer> weatherCode;
    }
}