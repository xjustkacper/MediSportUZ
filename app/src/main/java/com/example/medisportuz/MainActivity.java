
package com.example.medisportuz;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
/**
 * @brief Główna aktywność aplikacji pełniąca rolę kontenera dla fragmentów.
 * * Klasa ta zarządza dolnym paskiem nawigacyjnym (BottomNavigationView) i umożliwia
 * płynne przełączanie się pomiędzy kluczowymi modułami aplikacji: Stroną główną (Home),
 * Pogodą (Weather), Notatkami (Notes) oraz Ustawieniami (Settings).
 */
public class MainActivity extends AppCompatActivity {
    /**
     * @brief Inicjalizuje główny ekran aplikacji i konfiguruje nawigację.
     * * Metoda ta ustawia widok aktywności, przypisuje logikę do paska nawigacyjnego
     * i ładuje domyślny fragment (HomeFragment) przy pierwszym uruchomieniu aplikacji.
     * * @param savedInstanceState Stan zapisany z poprzedniej instancji aktywności.
     * Sprawdzenie (savedInstanceState == null) gwarantuje, że domyślny fragment
     * zostanie załadowany tylko przy pierwszym uruchomieniu, a nie np. po obrocie ekranu.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Load Home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_weather) {
                fragment = new WeatherFragment();
            } else if (itemId == R.id.nav_notes) {
                fragment = new NotesFragment();
            } else if (itemId == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });
    }
    /**
     * @brief Podmienia aktualnie wyświetlany fragment w kontenerze.
     * * Wykorzystuje narzędzie SupportFragmentManager do zainicjowania transakcji,
     * która podmienia zawartość elementu R.id.fragmentContainer na nowo wybrany fragment.
     * * @param fragment Instancja nowo utworzonego fragmentu (dziedziczącego po klasie Fragment),
     * która ma zostać wyświetlona na ekranie.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}