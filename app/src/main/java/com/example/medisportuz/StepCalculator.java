package com.example.medisportuz;

public class StepCalculator {

    /**
     * Oblicza spalone kalorie na podstawie liczby kroków.
     * Przyjmujemy średnio 0.04 kcal na krok.
     */
    public static float calculateCalories(int steps) {
        if (steps < 0) return 0;
        return steps * 0.04f;
    }

    /**
     * Oblicza dystans w kilometrach na podstawie liczby kroków.
     * Przyjmujemy średnią długość kroku 0.75 metra.
     */
    public static float calculateDistanceKm(int steps) {
        if (steps < 0) return 0;
        return (steps * 0.75f) / 1000f;
    }
}