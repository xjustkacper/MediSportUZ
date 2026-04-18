package com.example.medisportuz;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StepCalculatorTest {

    @Test
    public void testCalculateCalories() {
        assertEquals(0f, StepCalculator.calculateCalories(0), 0.001f);
        assertEquals(40f, StepCalculator.calculateCalories(1000), 0.001f);
        assertEquals(0f, StepCalculator.calculateCalories(-10), 0.001f);
    }

    @Test
    public void testCalculateDistanceKm() {
        assertEquals(0f, StepCalculator.calculateDistanceKm(0), 0.001f);
        assertEquals(0.75f, StepCalculator.calculateDistanceKm(1000), 0.001f);
        assertEquals(7.5f, StepCalculator.calculateDistanceKm(10000), 0.001f);
        assertEquals(0f, StepCalculator.calculateDistanceKm(-5), 0.001f);
    }
}