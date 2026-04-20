package com.example.medisportuz;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
/**
 * @brief Zestaw testów jednostkowych dla klasy WeatherCodeHelper.
 * * Weryfikuje poprawne mapowanie międzynarodowych, numerycznych kodów pogodowych WMO
 * (World Meteorological Organization) na zlokalizowane, czytelne dla użytkownika opisy tekstowe.
 * Testy podzielono tematycznie na główne kategorie zjawisk atmosferycznych.
 */
public class WeatherCodeHelperTest {
    /**
     * @brief Testuje mapowanie dla bezchmurnego nieba.
     * Kod WMO równy 0 jest wartością bazową, oznaczającą całkowity brak chmur.
     */
    @Test
    public void testGetDescription_ClearSky() {
        // --- GIVEN, WHEN, THEN ---
        assertEquals("Bezchmurnie", WeatherCodeHelper.getDescription(0));
    }
    /**
     * @brief Weryfikuje kody odpowiedzialne za różne stopnie zachmurzenia.
     * Testuje zarówno przypadki brzegowe dla częściowego zachmurzenia (1 i 2),
     * jak i kod oznaczający pełne zachmurzenie (3).
     */
    @Test
    public void testGetDescription_Cloudy() {
        // Kody 1 i 2 oznaczają przeważnie słoneczną pogodę z niewielkimi chmurami
        assertEquals("Częściowe zachmurzenie", WeatherCodeHelper.getDescription(1));
        assertEquals("Częściowe zachmurzenie", WeatherCodeHelper.getDescription(2));
        // Kod 3 to całkowite zakrycie nieba przez chmury
        assertEquals("Zachmurzenie", WeatherCodeHelper.getDescription(3));
    }
    /**
     * @brief Sprawdza poprawne rozpoznawanie mgły.
     * Według specyfikacji WMO, kody w okolicach 45-48 oznaczają mgłę i osadzającą się szadź.
     */
    @Test
    public void testGetDescription_Fog() {
        assertEquals("Mgła", WeatherCodeHelper.getDescription(45));
        assertEquals("Mgła", WeatherCodeHelper.getDescription(48));
    }
    /**
     * @brief Weryfikuje kody reprezentujące ciągłe opady deszczu.
     * Sprawdza typowe wartości dla deszczu słabego (61) oraz ostrego/intensywnego (65).
     */
    @Test
    public void testGetDescription_Rain() {
        assertEquals("Deszcz", WeatherCodeHelper.getDescription(61));
        assertEquals("Deszcz", WeatherCodeHelper.getDescription(65));
    }
    /**
     * @brief Testuje najgroźniejsze zjawiska pogodowe - burze.
     * Weryfikuje poprawność mapowania dla burzy o słabym lub umiarkowanym natężeniu (95)
     * oraz ekstremalnego przypadku brzegowego - burzy z ciężkim gradem (99 - najwyższy kod WMO).
     */
    @Test
    public void testGetDescription_Storm() {
        assertEquals("Burza", WeatherCodeHelper.getDescription(95));
        assertEquals("Burza z gradem", WeatherCodeHelper.getDescription(99));
    }
}