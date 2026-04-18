package com.example.medisportuz;

public class WeatherCodeHelper {

    /** Zwraca opis pogody po polsku na podstawie kodu WMO */
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

    /** Zwraca odpowiedni drawable na podstawie kodu WMO */
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