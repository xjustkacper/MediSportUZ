package com.example.medisportuz;
/**
 * @brief Klasa narzędziowa (Utility) do przeliczania danych związanych z krokomierzem.
 * * Zawiera statyczne metody pomocnicze, które pozwalają na szybką konwersję
 * liczby wykonanych kroków na spalone kalorie oraz pokonany dystans.
 */
public class StepCalculator {

    /**
     * @brief Oblicza szacunkową liczbę spalonych kalorii na podstawie wykonanych kroków.
     * * Do obliczeń przyjęto uśredniony współczynnik spalania wynoszący 0.04 kcal na jeden krok.
     * W przypadku przekazania wartości ujemnej, metoda bezpiecznie zwraca 0.
     *
     * @param steps Całkowita liczba wykonanych kroków.
     * @return Szacunkowa liczba spalonych kalorii wyrażona w kilokaloriach (kcal).
     */
    public static float calculateCalories(int steps) {
        if (steps < 0) return 0;
        return steps * 0.04f;
    }
    /**
     * @brief Oblicza pokonany dystans w kilometrach na podstawie liczby kroków.
     * * Do obliczeń przyjęto uśrednioną długość ludzkiego kroku wynoszącą 0.75 metra.
     * Wartość ta jest następnie dzielona przez 1000 w celu konwersji na kilometry.
     * W przypadku przekazania wartości ujemnej, metoda bezpiecznie zwraca 0.
     *
     * @param steps Całkowita liczba wykonanych kroków.
     * @return Pokonany dystans wyrażony w kilometrach (km).
     */
    public static float calculateDistanceKm(int steps) {
        if (steps < 0) return 0;
        return (steps * 0.75f) / 1000f;
    }
}