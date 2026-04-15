package com.example.medisportuz;

import java.util.List;

public class GeocodingResponse {
    public List<GeoResult> results;

    public static class GeoResult {
        public String name;
        public double latitude;
        public double longitude;
        public String country;
    }
}