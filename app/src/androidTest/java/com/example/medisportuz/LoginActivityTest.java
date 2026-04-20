package com.example.medisportuz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * @brief Zestaw testów interfejsu użytkownika (UI) dla ekranu logowania.
 * * Weryfikuje poprawność renderowania kluczowych elementów widoku
 * oraz działanie mechanizmu dynamicznej zmiany języka (lokalizacji) w aplikacji.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {
    /**
     * @brief Automatyczna reguła zarządzająca cyklem życia testowanej Aktywności.
     * * Zapewnia, że LoginActivity zostanie bezpiecznie uruchomione przed każdym
     * pojedynczym testem (@Test) i zamknięte po jego zakończeniu.
     */
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);
    /**
     * @brief Konfiguracja stanu początkowego (Setup) przed każdym testem.
     */
    @Before
    public void setUp() {
        // Wyloguj użytkownika przed testem, aby uniknąć automatycznego przekierowania do MainActivity
        FirebaseAuth.getInstance().signOut();
    }
    /**
     * @brief Weryfikuje, czy podstawowe elementy formularza logowania są widoczne na ekranie.
     * Tzw. "Smoke Test" upewniający się, że plik XML załadował się poprawnie i nie ma krytycznych błędów UI.
     */
    @Test
    public void testLoginUiElementsDisplayed() {
        onView(withId(R.id.loginEmailInput)).check(matches(isDisplayed()));
        onView(withId(R.id.loginPasswordInput)).check(matches(isDisplayed()));
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }
    /**
     * @brief Sprawdza mechanizm zmiany języka interfejsu (Lokalizacja) w czasie rzeczywistym.
     * Weryfikuje, czy kliknięcie odpowiedniej flagi podmienia zasoby tekstowe (String resources).
     */
    @Test
    public void testLanguageSwitch() {
        // Kliknij flagę EN
        onView(withId(R.id.flagEN)).perform(click());
        
        // Sprawdź czy tekst przycisku zmienił się na "Sign In"
        onView(withId(R.id.loginButton)).check(matches(withText("Sign In")));

        // Kliknij flagę PL
        onView(withId(R.id.flagPL)).perform(click());

        // Sprawdź czy tekst przycisku zmienił się z powrotem na "Zaloguj"
        onView(withId(R.id.loginButton)).check(matches(withText("Zaloguj")));
    }
}