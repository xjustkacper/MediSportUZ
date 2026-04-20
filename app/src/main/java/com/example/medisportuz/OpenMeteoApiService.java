package com.example.medisportuz;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
/**
 * @brief Interfejs klienta HTTP (Retrofit) służący do komunikacji z API Open-Meteo.
 * * Definiuje strukturę żądań sieciowych wykorzystywanych do pobierania danych pogodowych.
 * Biblioteka Retrofit na podstawie tego interfejsu automatycznie wygeneruje kod odpowiedzialny
 * za obsługę zapytań REST.
 */
public interface OpenMeteoApiService {
    /**
     * @brief Bazowy adres URL darmowego API Open-Meteo.
     * Wykorzystywany podczas inicjalizacji obiektu Retrofit w aplikacji.
     */
    String BASE_URL = "https://api.open-meteo.com/v1/";
    /**
     * @brief Pobiera aktualną oraz długoterminową prognozę pogody dla podanych współrzędnych.
     * * Metoda ta mapuje się na żądanie HTTP typu GET kierowane na endpoint "forecast"
     * (pełny adres to: https://api.open-meteo.com/v1/forecast). Parametry przekazane
     * do metody zostaną automatycznie dołączone do adresu URL jako parametry zapytania (Query).
     *
     * @param lat Szerokość geograficzna (np. 52.40 dla Poznania).
     * @param lon Długość geograficzna (np. 16.92 dla Poznania).
     * @param currentVars Lista zmiennych oddzielonych przecinkiem określających,
     * jakie dane o aktualnej pogodzie chcemy pobrać (np. "temperature_2m,windspeed_10m").
     * @param dailyVars Lista zmiennych oddzielonych przecinkiem określających,
     * jakie dzienne agregacje danych chcemy pobrać (np. "temperature_2m_max,temperature_2m_min").
     * @param timezone Strefa czasowa, według której mają zostać sformatowane dane dzienne
     * (zalecane np. "auto" lub "Europe/Warsaw").
     * @param forecastDays Liczba dni, na ile ma zostać wygenerowana prognoza (np. 7).
     * * @return Obiekt typu Call opakowujący odpowiedź serwera zmapowaną na obiekt OpenMeteoResponse.
     * Metodę tę można następnie wykonać asynchronicznie za pomocą wywołania .enqueue()
     * lub synchronicznie za pomocą .execute().
     */
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