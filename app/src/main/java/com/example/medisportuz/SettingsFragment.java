package com.example.medisportuz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
/**
 * @brief Fragment odpowiedzialny za ekran ustawień aplikacji.
 * * Klasa ta umożliwia użytkownikowi personalizację działania aplikacji,
 * w tym: zmianę celu dziennej liczby kroków, ustawienie numeru alarmowego (SOS),
 * przełączanie motywu (jasny/ciemny), wybór języka interfejsu oraz bezpieczne wylogowanie z konta.
 * Wszystkie ustawienia zapisywane są trwale przy użyciu mechanizmu SharedPreferences.
 */
public class SettingsFragment extends Fragment {
    /**
     * @brief Obiekt dostępu do lokalnych preferencji aplikacji.
     * Używany do zapisywania i odczytywania konfiguracji użytkownika.
     */
    private SharedPreferences sharedPreferences;
    /** Pole tekstowe do wprowadzania docelowej dziennej liczby kroków. */
    private TextInputEditText stepGoalInput;
    /** Pole tekstowe do wprowadzania numeru telefonu alarmowego (SOS). */
    private TextInputEditText sosPhoneInput;
    /**
     * @brief Inicjalizuje widok ustawień i podpina logikę pod poszczególne elementy interfejsu.
     * * Metoda ta ładuje aktualne ustawienia z SharedPreferences i wyświetla je w polach.
     * Definiuje również zachowanie dla przełączników (motyw), list wyboru (język)
     * oraz przycisków (zapis, wylogowanie).
     *
     * @param inflater Obiekt służący do "nadmuchania" widoku XML do postaci obiektu Java.
     * @param container Rodzicielski kontener, do którego zostanie dołączony widok.
     * @param savedInstanceState Stan zapisany z poprzedniej instancji (nieużywany w tym fragmencie).
     * @return Gotowy, zainicjalizowany widok fragmentu ustawień.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // --- Logika z brancha Krokomierz (Ustawienia celu kroków) ---
        // Inicjalizacja pliku preferencji o nazwie "MediSportPrefs" w trybie prywatnym
        sharedPreferences = requireActivity().getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        stepGoalInput = view.findViewById(R.id.settingsStepGoalInput);
        sosPhoneInput = view.findViewById(R.id.settingsSosPhoneInput);

        // Wczytanie i wyświetlenie aktualnego celu kroków (domyślnie 10 000)
        String currentGoal = sharedPreferences.getString("step_goal", "10000");
        stepGoalInput.setText(currentGoal);
        // Wczytanie i wyświetlenie zapisanego numeru SOS
        String currentSosPhone = sharedPreferences.getString("sos_phone", "");
        if (!currentSosPhone.isEmpty()) {
            sosPhoneInput.setText(currentSosPhone);
        }

        // --- Logika Motywu (Jasny/Ciemny) ---
        SwitchMaterial darkModeSwitch = view.findViewById(R.id.settingsDarkModeSwitch);
        // Wczytanie preferencji motywu (domyślnie ciemny - true)
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", true);
        darkModeSwitch.setChecked(isDarkMode);
        // Nasłuchiwacz zmian stanu przełącznika motywu
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // --- Logika Języka ---
        LinearLayout languageContainer = view.findViewById(R.id.settingsLanguageContainer);
        TextView languageValueText = view.findViewById(R.id.settingsLanguageValueText);
        // Wczytanie aktualnego języka (domyślnie polski - "pl")
        String currentLanguage = sharedPreferences.getString("language", "pl");
        if (currentLanguage.equals("en")) {
            languageValueText.setText("English");
        } else {
            languageValueText.setText("Polski");
        }
        // Obsługa kliknięcia w kontener języka - wyświetla okno dialogowe wyboru
        languageContainer.setOnClickListener(v -> {
            String[] languages = {"Polski", "English"};
            int checkedItem = currentLanguage.equals("en") ? 1 : 0;

            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.settings_profile_language)
                    .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                        String selectedLang = (which == 0) ? "pl" : "en";
                        if (!selectedLang.equals(currentLanguage)) {
                            sharedPreferences.edit().putString("language", selectedLang).apply();
                            
                            // Aplikuj język przez AndroidX
                            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(selectedLang);
                            AppCompatDelegate.setApplicationLocales(appLocale);
                        }
                        dialog.dismiss();
                    })
                    .show();
        });

        // --- Logika Zapisywania (Kroki i SOS) ---
        Button saveButton = view.findViewById(R.id.settingsSaveButton);
        saveButton.setOnClickListener(v -> {
            String goalText = stepGoalInput.getText() != null ? stepGoalInput.getText().toString().trim() : "";
            String phoneText = sosPhoneInput.getText() != null ? sosPhoneInput.getText().toString().trim() : "";

            // Walidacja wprowadzonego celu kroków
            if (goalText.isEmpty()) {
                goalText = "10000";
            }
            try {
                int goal = Integer.parseInt(goalText);
                if (goal <= 0) goalText = "10000";
            } catch (NumberFormatException e) {
                goalText = "10000";
            }
            // Zapis zwalidowanych danych
            sharedPreferences.edit()
                    .putString("step_goal", goalText)
                    .putString("sos_phone", phoneText)
                    .apply();

            // Inteligentna aktualizacja serwisu krokomierza
            int currentSteps = sharedPreferences.getInt("last_recorded_steps", 0);
            int newGoal = Integer.parseInt(goalText);
            // Jeśli użytkownik zwiększył cel i liczba kroków jest od niego mniejsza
            if (currentSteps < newGoal) {
                // Odblokuj flagę osiągnięcia celu
                sharedPreferences.edit().putBoolean("goal_reached_today", false).apply();
                Intent serviceIntent = new Intent(requireContext(), StepCounterService.class);
                androidx.core.content.ContextCompat.startForegroundService(requireContext(), serviceIntent);
            }

            Toast.makeText(getContext(), "Zapisano ustawienia", Toast.LENGTH_SHORT).show();
        });

        // --- Logika wylogowania---
        Button logoutButton = view.findViewById(R.id.settingsLogoutButton);
        logoutButton.setOnClickListener(v -> {
            // Bezpieczne wylogowanie sesji na serwerach Firebase
            FirebaseAuth.getInstance().signOut();
            // Przekierowanie do ekranu logowania z wyczyszczeniem stosu aktywności (backstack)
            // Zapobiega to powrotowi do aplikacji po wciśnięciu sprzętowego przycisku "Wstecz"
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
