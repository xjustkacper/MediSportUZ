package com.example.medisportuz;

import com.google.gson.annotations.SerializedName;
import java.util.List;
/**
 * @brief Główny model danych reprezentujący odpowiedź z API Open-Meteo.
 * * Klasa ta służy do automatycznej deserializacji struktury JSON zwracanej przez serwer
 * na obiekty języka Java za pomocą biblioteki Gson. Zawiera zagnieżdżone obiekty dla
 * warunków bieżących oraz prognozy wielodniowej.
 */
public class OpenMeteoResponse {
    /**
     * @brief Obiekt zawierający dane o aktualnych warunkach pogodowych.
     * Zmapowany z klucza "current" w odpowiedzi JSON.
     */
    @SerializedName("current")
    public Current current;
    /**
     * @brief Obiekt zawierający listy danych z prognozą na kolejne dni.
     * Zmapowany z klucza "daily" w odpowiedzi JSON.
     */
    @SerializedName("daily")
    public Daily daily;
    /**
     * @brief Klasa wewnętrzna modelująca węzeł bieżącej pogody (Current Weather).
     * Przechowuje pojedyncze wartości z określonego momentu (zazwyczaj chwili zapytania).
     */
    public static class Current {
        /**
         * @brief Aktualna temperatura powietrza (na wysokości 2m).
         */
        @SerializedName("temperature_2m")
        public float temperature;
        /**
         * @brief Kod stanu pogody według standardu WMO (np. 0 = bezchmurnie, 3 = pochmurno).
         */
        @SerializedName("weather_code")
        public int weatherCode;
        /**
         * @brief Aktualna prędkość wiatru (na wysokości 10m).
         */
        @SerializedName("wind_speed_10m")
        public float windSpeed;
        /**
         * @brief Względna wilgotność powietrza wyrażona w procentach (na wysokości 2m).
         */
        @SerializedName("relative_humidity_2m")
        public int humidity;
    }
    /**
     * @brief Klasa wewnętrzna modelująca węzeł prognozy dziennej (Daily Forecast).
     * Zgodnie ze specyfikacją API Open-Meteo, dane dzienne są zwracane jako równoległe
     * tablice (listy). Element pod indeksem 'i' we wszystkich listach odnosi się do tego samego dnia.
     */
    public static class Daily {
        /**
         * @brief Lista dat dla prognozowanych dni.
         * Format ciągu znaków to standardowo "YYYY-MM-DD" (np. "2025-03-25").
         */
        @SerializedName("time")
        public List<String> time;             // np. ["2025-03-25", "2025-03-26", ...]
        /**
         * @brief Lista maksymalnych temperatur (na wysokości 2m) dla poszczególnych dni.
         */
        @SerializedName("temperature_2m_max")
        public List<Float> tempMax;
        /**
         * @brief Lista minimalnych temperatur (na wysokości 2m) dla poszczególnych dni.
         */
        @SerializedName("temperature_2m_min")
        public List<Float> tempMin;
        /**
         * @brief Lista dominujących kodów pogodowych (WMO) dla poszczególnych dni.
         */
        @SerializedName("weather_code")
        public List<Integer> weatherCode;
    }
}