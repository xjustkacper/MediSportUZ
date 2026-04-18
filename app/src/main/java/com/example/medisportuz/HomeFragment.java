package com.example.medisportuz;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView stepCountTextView;
    private TextView stepsRemainingTextView;
    private TextView stepGoalTextView;
    private TextView distanceTextView;
    private TextView caloriesTextView;
    private CircularProgressIndicator stepProgressBar;
    private AdView adView;

    private int stepGoal = 10000;
    private SharedPreferences sharedPreferences;

    private final BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("STEP_UPDATE".equals(intent.getAction())) {
                int steps = intent.getIntExtra("steps", 0);
                updateUI(steps);
            }
        }
    };

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean activityGranted = result.getOrDefault(Manifest.permission.ACTIVITY_RECOGNITION, false);
                if (activityGranted) {
                    startStepService();
                } else {
                    Toast.makeText(getContext(), "Brak uprawnień do kroków!", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Logika z brancha test (Firebase Greeting) ---
        TextView greeting = view.findViewById(R.id.homeGreeting);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Najpierw ustaw awaryjnie z obiektu Auth (zanim dane spłyną z bazy)
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                greeting.setText(getString(R.string.home_greeting, user.getDisplayName()));
            }

            // Następnie pobierz najświeższe dane z Firestore
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String imie = document.getString("imię");
                                if (imie != null && !imie.isEmpty()) {
                                    greeting.setText(getString(R.string.home_greeting, imie));
                                }
                            }
                        }
                    });
        }

        // --- Logika z brancha Krokomierz (Step Counter UI) ---
        stepCountTextView = view.findViewById(R.id.homeStepCount);
        stepsRemainingTextView = view.findViewById(R.id.homeStepsRemaining);
        stepGoalTextView = view.findViewById(R.id.homeStepGoalText);
        distanceTextView = view.findViewById(R.id.homeDistance);
        caloriesTextView = view.findViewById(R.id.homeCalories);
        stepProgressBar = view.findViewById(R.id.homeStepProgressBar);

        sharedPreferences = requireActivity().getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        loadStepGoal();

        // Load last recorded steps to prevent UI flickering to 0
        int lastSteps = sharedPreferences.getInt("last_recorded_steps", 0);
        updateUI(lastSteps);

        // --- Google AdMob Banner ---
        MobileAds.initialize(requireContext(), initializationStatus -> {});
        adView = view.findViewById(R.id.homeAdBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // --- SOS Message ---
        FrameLayout sosButton = view.findViewById(R.id.homeSosButton);

        // Ustawiamy nasłuchiwacz kliknięć
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSosWithLocation();
            }
        });

        return view;
    }

    private void sendSosWithLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            Toast.makeText(getContext(), "Brak uprawnień do lokalizacji! Spróbuj ponownie po ich przyznaniu.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdź czy lokalizacja jest włączona w systemie
        android.location.LocationManager lm = (android.location.LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {}

        if (!gpsEnabled) {
            showLocationSettingsDialog();
            return;
        }

        Toast.makeText(getContext(), "Pobieram lokalizację...", Toast.LENGTH_SHORT).show();

        com.google.android.gms.location.FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(requireActivity());

        // KROK 1: Pobierz ostatnią znaną lokalizację (jest natychmiastowa)
        client.getLastLocation().addOnSuccessListener(lastLocation -> {
            // Jeśli ostatnia lokalizacja jest świeża (np. sprzed minuty), wyślij ją od razu
            if (lastLocation != null && (System.currentTimeMillis() - lastLocation.getTime() < 60000)) {
                sendSosSms(lastLocation);
            } else {
                // KROK 2: Jeśli nie ma świeżej "ostatniej", wymuś pobranie nowej
                CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setDurationMillis(10000) // Czekaj max 10 sekund
                        .build();

                client.getCurrentLocation(request, null).addOnCompleteListener(task -> {
                    Location freshLocation = task.isSuccessful() ? task.getResult() : null;
                    if (freshLocation != null) {
                        sendSosSms(freshLocation);
                    } else if (lastLocation != null) {
                        // Jeśli nowa się nie udała, użyj starej (lepsze to niż nic)
                        sendSosSms(lastLocation);
                    } else {
                        sendSms("Achtung! Ich brauche Hilfe! (Lokalizacja niedostępna - sprawdź GPS)");
                    }
                });
            }
        }).addOnFailureListener(e -> {
            sendSms("Achtung! Ich brauche Hilfe! (Błąd GPS)");
        });
    }

    private void showLocationSettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lokalizacja jest wyłączona")
                .setMessage("Aby wysłać Twoje współrzędne w wezwaniu SOS, musisz włączyć lokalizację (GPS) w ustawieniach telefonu.")
                .setPositiveButton("Ustawienia", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Wyślij bez GPS", (dialog, which) -> {
                    sendSms("Achtung! Ich brauche Hilfe! (Lokalizacja wyłączona przez użytkownika)");
                })
                .show();
    }

    private void sendSosSms(Location location) {
        String message = String.format(Locale.US,
                "Achtung! Ich brauche Hilfe!\nMeine Koordinaten: %.6f, %.6f\nhttps://www.google.com/maps?q=%.6f,%.6f",
                location.getLatitude(), location.getLongitude(),
                location.getLatitude(), location.getLongitude());
        sendSms(message);
    }

    private void sendSms(String message) {
        SharedPreferences sharedPrefs = requireActivity().getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        String phoneNumber = sharedPrefs.getString("sos_phone", "0");

        Uri uri = Uri.parse("smsto:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }

    private void loadStepGoal() {
        String goalStr = sharedPreferences.getString("step_goal", "10000");
        try {
            stepGoal = Integer.parseInt(goalStr);
        } catch (NumberFormatException e) {
            stepGoal = 10000;
        }
    }

    private void checkPermissionsAndStartService() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (permissionsNeeded.isEmpty()) {
            startStepService();
        } else {
            requestPermissionsLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    private void startStepService() {
        Intent serviceIntent = new Intent(requireContext(), StepCounterService.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    private void updateUI(int steps) {
        if (stepCountTextView != null) {
            stepCountTextView.setText(getString(R.string.home_steps_count_format, steps));

            int remaining = stepGoal - steps;
            if (remaining < 0) remaining = 0;
            stepsRemainingTextView.setText(getString(R.string.home_steps_remaining_format, remaining));

            // Update goal text dynamically
            stepGoalTextView.setText(getString(R.string.home_steps_goal_format, stepGoal));

            // Update circular progress bar
            stepProgressBar.setMax(stepGoal);
            stepProgressBar.setProgress(Math.min(steps, stepGoal));

            float distanceKm = (steps * 0.762f) / 1000f;
            float calories = steps * 0.04f;

            distanceTextView.setText(getString(R.string.home_distance_format, distanceKm));
            caloriesTextView.setText(getString(R.string.home_calories_format, calories));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStepGoal();

        // Uruchom serwis tylko jeśli cel nie został jeszcze osiągnięty
        boolean goalReached = sharedPreferences.getBoolean("goal_reached_today", false);
        if (!goalReached) {
            checkPermissionsAndStartService();
        }

        // Update UI with latest saved value immediately on resume
        int lastSteps = sharedPreferences.getInt("last_recorded_steps", 0);
        updateUI(lastSteps);

        // Register receiver for updates from service
        IntentFilter filter = new IntentFilter("STEP_UPDATE");
        // Using ContextCompat to handle RECEIVER_NOT_EXPORTED on all API levels for Android 14+ security
        ContextCompat.registerReceiver(requireContext(), stepReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireActivity().unregisterReceiver(stepReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered", e);
        }
    }
}
