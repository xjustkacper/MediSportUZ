package com.example.medisportuz;
/**
 * @brief Klasa narzędziowa (Utility) do interpretacji kodów pogodowych WMO.
 * * Tłumaczy kody numeryczne zwracane przez interfejsy API pogodowe (np. Open-Meteo),
 * oparte na standardzie Światowej Organizacji Meteorologicznej (WMO),
 * na zlokalizowane opisy tekstowe oraz odpowiednie zasoby graficzne (ikony).
 */
public class WeatherCodeHelper {

    /**
     * @brief Zwraca opis stanu pogody w języku polskim na podstawie kodu WMO.
     * * Dokonuje grupowania kodów numerycznych w czytelne dla użytkownika kategorie,
     * takie jak bezchmurnie, deszcz, śnieg czy burza.
     *
     * @param code Kod pogodowy WMO (liczba całkowita z przedziału 0-99).
     * @return Zrozumiały ciąg znaków (String) opisujący aktualne warunki atmosferyczne.
     */
    public static String getDescription(int code) {
        if (code == 0)             return "Bezchmurnie";
        if (code <= 2)             return "Częściowe zachmurzenie";
        if (code == 3)             return "Zachmurzenie";
        if (code <= 49)            return "Mgła";
        if (code <= 59)            return "Mżawka";
        if (code <= 69)            return "Deszcz";
        if (code <= 79)            return "Śnieg";
        if (code <= 84)            return "Przelotny deszcz";
        if (code <= 95)            return "Burza";
        return "Burza z gradem";
    }

    /**
     * @brief Zwraca identyfikator zasobu graficznego (ikony) odpowiadającego danemu kodowi WMO.
     * * Mapuje stan pogody na uproszczony zbiór ikon wektorowych dostępnych w aplikacji.
     * Wiele podobnych kodów (np. różne stopnie zachmurzenia czy intensywności opadów)
     * jest przypisywanych do tej samej, uniwersalnej ikony.
     *
     * @param code Kod pogodowy WMO (liczba całkowita z przedziału 0-99).
     * @return Identyfikator zasobu (R.drawable.id) reprezentujący wizualnie dany stan pogody.
     */
    public static int getIcon(int code) {
        if (code == 0)             return R.drawable.ic_weather_sun;
        if (code <= 2)             return R.drawable.ic_weather_sun;    // częściowe słońce
        if (code <= 3)             return R.drawable.ic_weather_cloud;
        if (code <= 49)            return R.drawable.ic_weather_cloud;  // mgła
        if (code <= 69)            return R.drawable.ic_weather_rain;
        if (code <= 79)            return R.drawable.ic_weather_snow;
        if (code <= 84)            return R.drawable.ic_weather_rain;
        return R.drawable.ic_weather_storm;
    }
}