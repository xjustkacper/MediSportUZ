package com.example.medisportuz;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView stepCountTextView;
    private TextView stepsRemainingTextView;
    private TextView stepGoalTextView;
    private TextView distanceTextView;
    private TextView caloriesTextView;
    private CircularProgressIndicator stepProgressBar;

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

        return view;
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
        checkPermissionsAndStartService();

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
