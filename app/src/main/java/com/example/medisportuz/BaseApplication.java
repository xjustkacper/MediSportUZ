package com.example.medisportuz;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.firebase.FirebaseApp;
/**
 * @brief Niestandardowa klasa główna aplikacji.
 * * Klasa ta rozszerza domyślną klasę android.app.Application i jest uruchamiana
 * przed uruchomieniem jakichkolwiek innych komponentów aplikacji (takich jak Activity czy Service).
 * Służy do przeprowadzania jednorazowej, globalnej inicjalizacji bibliotek (np. Firebase)
 * oraz przywracania preferencji użytkownika (motyw, język) na poziomie całej aplikacji.
 */
public class BaseApplication extends Application {
    /**
     * @brief Wywoływana automatycznie podczas startu aplikacji.
     * * Metoda ta odpowiada za:
     * - Zainicjalizowanie środowiska Firebase.
     * - Odczytanie ustawień użytkownika z SharedPreferences ("MediSportPrefs").
     * - Zaaplikowanie odpowiedniego motywu (jasnego lub ciemnego).
     * - Ustawienie preferowanego języka (Locale) dla całej aplikacji.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inicjalizacja Firebase
        FirebaseApp.initializeApp(this);
        
        SharedPreferences prefs = getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        
        // --- 1. Motyw (Domyslnie ciemny = true) ---
        boolean isDarkMode = prefs.getBoolean("dark_mode", true);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        // --- 2. Jezyk (Domyslnie polski = "pl") ---
        String lang = prefs.getString("language", "pl");
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}
