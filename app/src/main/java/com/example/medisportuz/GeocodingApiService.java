package com.example.medisportuz;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
/**
 * @brief Interfejs Retrofit do komunikacji z API geolokalizacyjnym Open-Meteo.
 * * Klasa ta definiuje punkty końcowe (endpoints) dla usługi geolokalizacji.
 * Pozwala na wyszukiwanie informacji o lokacjach (takich jak współrzędne geograficzne,
 * kraj, strefa czasowa) na podstawie nazwy miasta. Wykorzystuje bibliotekę Retrofit
 * do mapowania zapytań HTTP na wywołania metod w języku Java.
 */
public interface GeocodingApiService {
    /**
     * @brief Bazowy adres URL dla usługi geolokalizacyjnej Open-Meteo.
     */
    String BASE_URL = "https://geocoding-api.open-meteo.com/v1/";
    /**
     * @brief Wyszukuje szczegóły lokalizacji na podstawie nazwy miasta.
     * * Wykonuje asynchroniczne lub synchroniczne zapytanie GET do punktu końcowego "search"
     * z odpowiednimi parametrami w adresie URL.
     * * @param name Nazwa wyszukiwanego miasta (np. "Poznań" lub "Berlin").
     * @param count Maksymalna liczba wyników, które ma zwrócić API. Przydatne do
     * ograniczenia wielkości odpowiedzi sieciowej.
     * @param language Dwuliterowy kod języka, w którym mają zostać zwrócone nazwy
     * (np. "pl" dla języka polskiego, "en" dla angielskiego).
     * @return Obiekt Call biblioteki Retrofit opakowujący zdeserializowaną odpowiedź
     * typu GeocodingResponse, reprezentującą listę znalezionych lokalizacji.
     */
    @GET("search")
    Call<GeocodingResponse> searchCity(
            @Query("name")     String name,
            @Query("count")    int count,
            @Query("language") String language
    );
}