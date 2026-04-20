package com.example.medisportuz;

import java.util.List;
/**
 * @brief Model danych reprezentujący główną odpowiedź z API geolokalizacyjnego.
 * * Klasa ta służy do automatycznego mapowania struktury JSON zwróconej przez serwer
 * (np. Open-Meteo) na obiekt języka Java. Zawiera listę dopasowanych wyników
 * wyszukiwania dla zadanego zapytania.
 */
public class GeocodingResponse {
    /**
     * @brief Lista znalezionych lokalizacji pasujących do wyszukiwanej nazwy.
     * W przypadku braku dopasowań w bazie API, lista ta może być pusta (lub wynosić null,
     * zależnie od użytego parsera JSON).
     */
    public List<GeoResult> results;
    /**
     * @brief Wewnętrzna klasa modelowa reprezentująca pojedynczy wynik geolokalizacyjny.
     * * Przechowuje podstawowe dane przestrzenne i administracyjne dla konkretnego
     * miejsca zwróconego przez API.
     */
    public static class GeoResult {
        /**
         * @brief Nazwa znalezionej lokalizacji (zazwyczaj nazwa miasta lub regionu).
         */
        public String name;
        /**
         * @brief Szerokość geograficzna współrzędnych miejsca, wyrażona w formacie dziesiętnym.
         */
        public double latitude;
        /**
         * @brief Długość geograficzna współrzędnych miejsca, wyrażona w formacie dziesiętnym.
         */
        public double longitude;
        /**
         * @brief Nazwa kraju, do którego przynależy wyszukana lokalizacja.
         */
        public String country;
    }
}