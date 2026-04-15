package com.example.medisportuz;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationServices;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // ── Aktualna pogoda ──
    private TextView cityName, temperature, description;
    private ImageView weatherIcon;

    // ── Prognoza Dzień 1 ──
    private TextView day1Name, day1TempMax, day1TempMin;
    private ImageView day1Icon;

    // ── Prognoza Dzień 2 ──
    private TextView day2Name, day2TempMax, day2TempMin;
    private ImageView day2Icon;

    // ── Prognoza Dzień 3 ──
    private TextView day3Name, day3TempMax, day3TempMin;
    private ImageView day3Icon;

    // ── Pasek wyszukiwania ──
    private EditText searchInput;
    private ImageView searchButton;
    private Button gpsButton;

    // ── Retrofit ──
    private OpenMeteoApiService weatherApi;
    private GeocodingApiService geoApi;

    // ── Zapamiętane miasto (do wyświetlenia nazwy po geokodowaniu) ──
    private String currentCityName = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        initViews(view);
        initRetrofit();
        setupListeners();

        // Domyślnie Warszawa
        geocodeAndFetch("Warsaw");

        return view;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Inicjalizacja
    // ────────────────────────────────────────────────────────────────────────

    private void initViews(View view) {
        cityName    = view.findViewById(R.id.weatherCityName);
        temperature = view.findViewById(R.id.weatherTemperature);
        description = view.findViewById(R.id.weatherDescription);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        searchInput = view.findViewById(R.id.weatherSearchInput);
        searchButton = view.findViewById(R.id.weatherSearchButton);
        gpsButton   = view.findViewById(R.id.weatherGpsButton);

        // Prognoza — zakładamy, że dodasz id do XML (patrz uwaga poniżej)
        day1Name    = view.findViewById(R.id.forecastDay1Name);
        day1TempMax = view.findViewById(R.id.forecastDay1TempMax);
        day1TempMin = view.findViewById(R.id.forecastDay1TempMin);
        day1Icon    = view.findViewById(R.id.forecastDay1Icon);

        day2Name    = view.findViewById(R.id.forecastDay2Name);
        day2TempMax = view.findViewById(R.id.forecastDay2TempMax);
        day2TempMin = view.findViewById(R.id.forecastDay2TempMin);
        day2Icon    = view.findViewById(R.id.forecastDay2Icon);

        day3Name    = view.findViewById(R.id.forecastDay3Name);
        day3TempMax = view.findViewById(R.id.forecastDay3TempMax);
        day3TempMin = view.findViewById(R.id.forecastDay3TempMin);
        day3Icon    = view.findViewById(R.id.forecastDay3Icon);
    }

    private void initRetrofit() {
        weatherApi = new Retrofit.Builder()
                .baseUrl(OpenMeteoApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoApiService.class);

        geoApi = new Retrofit.Builder()
                .baseUrl(GeocodingApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeocodingApiService.class);
    }

    private void setupListeners() {
        searchButton.setOnClickListener(v -> searchCity());

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                searchCity();
                return true;
            }
            return false;
        });

        gpsButton.setOnClickListener(v -> fetchByGps());
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Geokodowanie → pobieranie pogody
    // ────────────────────────────────────────────────────────────────────────

    private void searchCity() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Wpisz nazwę miasta", Toast.LENGTH_SHORT).show();
            return;
        }
        geocodeAndFetch(query);
    }

    /** Krok 1: zamień nazwę miasta na współrzędne */
    private void geocodeAndFetch(String cityQuery) {
        geoApi.searchCity(cityQuery, 1, "pl").enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call,
                                   @NonNull Response<GeocodingResponse> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().results != null
                        && !response.body().results.isEmpty()) {

                    GeocodingResponse.GeoResult result = response.body().results.get(0);
                    currentCityName = result.name;
                    fetchWeather(result.latitude, result.longitude);

                } else {
                    Toast.makeText(getContext(),
                            "Nie znaleziono miasta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(),
                        "Błąd połączenia: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Krok 2: pobierz pogodę dla danych współrzędnych */
    private void fetchWeather(double lat, double lon) {
        weatherApi.getForecast(
                lat, lon,
                "temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m",
                "temperature_2m_max,temperature_2m_min,weather_code",
                "auto",   // automatyczna strefa czasowa
                4         // dziś + 3 dni prognozy
        ).enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenMeteoResponse> call,
                                   @NonNull Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(getContext(),
                            "Błąd pobierania danych", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(),
                        "Błąd API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────────
    //  GPS
    // ────────────────────────────────────────────────────────────────────────

    private void fetchByGps() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        LocationServices.getFusedLocationProviderClient(requireActivity())
                .getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentCityName = "Twoja lokalizacja";
                        fetchWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(getContext(),
                                "Nie można pobrać lokalizacji GPS", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchByGps();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Aktualizacja UI
    // ────────────────────────────────────────────────────────────────────────

    private void updateUI(OpenMeteoResponse data) {
        // ── Aktualna pogoda ──
        cityName.setText(currentCityName);
        temperature.setText(String.format(Locale.getDefault(),
                "%d°C", Math.round(data.current.temperature)));
        description.setText(WeatherCodeHelper.getDescription(data.current.weatherCode));
        weatherIcon.setImageResource(WeatherCodeHelper.getIcon(data.current.weatherCode));

        // ── Prognoza (indeks 1, 2, 3 — pomijamy dziś = 0) ──
        if (data.daily != null && data.daily.time != null && data.daily.time.size() >= 4) {
            updateForecastDay(day1Name, day1Icon, day1TempMax, day1TempMin, data, 1);
            updateForecastDay(day2Name, day2Icon, day2TempMax, day2TempMin, data, 2);
            updateForecastDay(day3Name, day3Icon, day3TempMax, day3TempMin, data, 3);
        }
    }

    private void updateForecastDay(TextView nameView, ImageView iconView,
                                   TextView maxView, TextView minView,
                                   OpenMeteoResponse data, int index) {
        // Nazwa dnia tygodnia z daty "YYYY-MM-DD"
        String dateStr = data.daily.time.get(index);
        LocalDate date = LocalDate.parse(dateStr);
        String dayName = date.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, new Locale("pl"));

        int code = data.daily.weatherCode.get(index);

        nameView.setText(dayName);
        iconView.setImageResource(WeatherCodeHelper.getIcon(code));
        maxView.setText(String.format(Locale.getDefault(),
                "%d°C", Math.round(data.daily.tempMax.get(index))));
        minView.setText(String.format(Locale.getDefault(),
                "%d°C", Math.round(data.daily.tempMin.get(index))));
    }
}