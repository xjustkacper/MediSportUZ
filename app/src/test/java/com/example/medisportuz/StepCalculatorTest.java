package com.example.medisportuz;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
/**
 * @brief Zestaw testów jednostkowych dla algorytmów przeliczania kroków (StepCalculator).
 * * Sprawdza poprawność obliczeń matematycznych dla spalonych kalorii oraz pokonanego dystansu.
 * Weryfikuje zachowanie metod zarówno dla standardowych danych (Happy Path),
 * jak i przypadków brzegowych (Edge Cases), takich jak zero czy wartości ujemne.
 */
public class StepCalculatorTest {
    /**
     * @brief Testuje algorytm przeliczania kroków na spalone kalorie.
     * Oczekiwany przelicznik to 0.04 kcal na każdy wykonany krok.
     */
    @Test
    public void testCalculateCalories() {
        // --- GIVEN, WHEN, THEN (Testowanie różnych scenariuszy w jednej metodzie) ---

        // Scenariusz 1: Brak aktywności. 0 kroków powinno zwrócić 0 spalonych kalorii.
        assertEquals(0f, StepCalculator.calculateCalories(0), 0.001f);
        // Scenariusz 2: Standardowy spacer. 1000 kroków * 0.04 kcal = 40 kcal.
        assertEquals(40f, StepCalculator.calculateCalories(1000), 0.001f);
        // Scenariusz 3: Zabezpieczenie przed błędem (Edge Case).
        // Wartość ujemna nie może powodować "odzyskiwania" kalorii i powinna zwrócić 0.
        assertEquals(0f, StepCalculator.calculateCalories(-10), 0.001f);
    }
    /**
     * @brief Testuje konwersję liczby kroków na pokonany dystans w kilometrach.
     * Oczekiwany przelicznik to 0.75 metra (0.00075 km) na każdy wykonany krok.
     */
    @Test
    public void testCalculateDistanceKm() {
        // --- GIVEN, WHEN, THEN ---

        // Scenariusz 1: Brak aktywności to zerowy dystans.
        assertEquals(0f, StepCalculator.calculateDistanceKm(0), 0.001f);
        // Scenariusz 2: Krótki spacer. 1000 kroków * 0.75 m = 750 m, czyli 0.75 km.
        assertEquals(0.75f, StepCalculator.calculateDistanceKm(1000), 0.001f);
        // Scenariusz 3: Realizacja dziennego celu. 10 000 kroków to dokładnie 7.5 km.
        assertEquals(7.5f, StepCalculator.calculateDistanceKm(10000), 0.001f);
        // Scenariusz 4: Zabezpieczenie przed błędem (Edge Case).
        // Wartość ujemna powinna zostać zignorowana i zwrócić 0
        assertEquals(0f, StepCalculator.calculateDistanceKm(-5), 0.001f);
    }
}