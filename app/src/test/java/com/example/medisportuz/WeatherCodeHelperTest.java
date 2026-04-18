package com.example.medisportuz;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class WeatherCodeHelperTest {

    @Test
    public void testGetDescription_ClearSky() {
        assertEquals("Bezchmurnie", WeatherCodeHelper.getDescription(0));
    }

    @Test
    public void testGetDescription_Cloudy() {
        assertEquals("Częściowe zachmurzenie", WeatherCodeHelper.getDescription(1));
        assertEquals("Częściowe zachmurzenie", WeatherCodeHelper.getDescription(2));
        assertEquals("Zachmurzenie", WeatherCodeHelper.getDescription(3));
    }

    @Test
    public void testGetDescription_Fog() {
        assertEquals("Mgła", WeatherCodeHelper.getDescription(45));
        assertEquals("Mgła", WeatherCodeHelper.getDescription(48));
    }

    @Test
    public void testGetDescription_Rain() {
        assertEquals("Deszcz", WeatherCodeHelper.getDescription(61));
        assertEquals("Deszcz", WeatherCodeHelper.getDescription(65));
    }

    @Test
    public void testGetDescription_Storm() {
        assertEquals("Burza", WeatherCodeHelper.getDescription(95));
        assertEquals("Burza z gradem", WeatherCodeHelper.getDescription(99));
    }
}