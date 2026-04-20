package com.example.medisportuz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * @brief Test UI (End-to-End) weryfikujący poprawność głównego procesu logowania.
 * * Wykorzystuje framework Espresso do symulacji interakcji użytkownika na ekranie.
 * Sprawdza pełną ścieżkę: od wpisania danych, przez obsługę dynamicznej CAPTCHY,
 * aż po asynchroniczną autoryzację w Firebase i przejście do głównego ekranu aplikacji.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginFlowTest {

    @Test
    public void testLoginAndVerifyStepsText() {
        // --- GIVEN (Przygotowanie stanu początkowego) ---

        // Wymuszenie czystego stanu: wylogowanie z Firebase.
        // Zapobiega to sytuacji, w której aplikacja automatycznie przeskakuje
        // do MainActivity na starcie (auto-login), uniemożliwiając wykonanie testu logowania.
        FirebaseAuth.getInstance().signOut();

        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            // --- WHEN (Akcje wykonywane przez użytkownika) ---

            // 1. Wprowadzenie adresu e-mail i schowanie klawiatury
            onView(withId(R.id.loginEmailInput))
                    .perform(replaceText("liga.lol12345@gmail.com"), closeSoftKeyboard());

            // 2. Wpisz hasło
            onView(withId(R.id.loginPasswordInput))
                    .perform(replaceText("haslo123"), closeSoftKeyboard());

            // 3. Pobierz tekst Captcha z ekranu
            String captcha = getTextFromView(withId(R.id.loginCaptchaText));

            // 4. Wpisz pobraną Captcha
            onView(withId(R.id.loginCaptchaInput))
                    .perform(replaceText(captcha), closeSoftKeyboard());

            // 5. Kliknij przycisk logowania
            onView(withId(R.id.loginButton)).perform(click());

            // --- THEN (Oczekiwane rezultaty) ---

            // Ponieważ logowanie przez Firebase jest operacją asynchroniczną (wymaga sieci),
            // musimy zaczekać, aż aplikacja zmieni ekran na główny.
            // Szukamy tekstu "kroków", który jest charakterystyczny dla ekranu krokomierza.
            waitForView(withText(containsString("kroków")), 10000);

            // Ostateczna asercja (sprawdzenie), czy po upływie czasu element faktycznie jest widoczny.
            onView(withText(containsString("kroków"))).check(matches(isDisplayed()));
        }
    }
    /**
     * @brief Metoda pomocnicza implementująca mechanizm "Polling" (aktywne czekanie).
     * * Zamiast używać skomplikowanych IdlingResources, ta funkcja cyklicznie sprawdza
     * widoczność elementu. Jest to przydatne przy testowaniu operacji asynchronicznych (np. zapytania sieciowe).
     *
     * @param viewMatcher Dopasowywacz (Matcher) wskazujący na oczekiwany element UI.
     * @param timeout Maksymalny czas oczekiwania w milisekundach.
     */
    private void waitForView(final Matcher<View> viewMatcher, final long timeout) {
        final long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime) {
            try {
                onView(viewMatcher).check(matches(isDisplayed()));
                return;
            } catch (Exception e) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
        onView(viewMatcher).check(matches(isDisplayed()));
    }
    /**
     * @brief Metoda pomocnicza (Custom ViewAction) wyciągająca tekst z kontrolki TextView.
     * * Domyślnie Espresso służy do weryfikacji (asercji) tekstu, a nie jego pobierania do zmiennych.
     * Ta akcja omija to ograniczenie, co pozwala na testowanie dynamicznych elementów jak CAPTCHA.
     */
    private String getTextFromView(final Matcher<View> matcher) {
        final String[] stringHolder = {null};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                stringHolder[0] = ((TextView) view).getText().toString();
            }
        });
        return stringHolder[0];
    }
}
