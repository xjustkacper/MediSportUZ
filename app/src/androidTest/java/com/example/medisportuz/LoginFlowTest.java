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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginFlowTest {

    @Test
    public void testLoginAndVerifyStepsText() {
        // Wyloguj przed startem, aby uniknąć auto-przekierowania
        FirebaseAuth.getInstance().signOut();

        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            // 1. Wpisz e-mail
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

            // 6. Czekaj na element zawierający napis "kroków" (max 10 sekund)
            waitForView(withText(containsString("kroków")), 10000);

            // 7. Weryfikacja końcowa
            onView(withText(containsString("kroków"))).check(matches(isDisplayed()));
        }
    }

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
