package com.example.medisportuz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private TextInputEditText stepGoalInput;
    private TextInputEditText sosPhoneInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // --- Logika z brancha Krokomierz (Ustawienia celu kroków) ---
        sharedPreferences = requireActivity().getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        stepGoalInput = view.findViewById(R.id.settingsStepGoalInput);
        sosPhoneInput = view.findViewById(R.id.settingsSosPhoneInput);

        // Load current values
        String currentGoal = sharedPreferences.getString("step_goal", "10000");
        stepGoalInput.setText(currentGoal);

        String currentSosPhone = sharedPreferences.getString("sos_phone", "");
        if (!currentSosPhone.isEmpty()) {
            sosPhoneInput.setText(currentSosPhone);
        }

        // Save button - saves step goal and SOS phone at once
        Button saveButton = view.findViewById(R.id.settingsSaveButton);
        saveButton.setOnClickListener(v -> {
            String goalText = stepGoalInput.getText() != null ? stepGoalInput.getText().toString().trim() : "";
            String phoneText = sosPhoneInput.getText() != null ? sosPhoneInput.getText().toString().trim() : "";

            // Validate step goal
            if (goalText.isEmpty()) {
                goalText = "10000";
            }
            try {
                int goal = Integer.parseInt(goalText);
                if (goal <= 0) goalText = "10000";
            } catch (NumberFormatException e) {
                goalText = "10000";
            }

            sharedPreferences.edit()
                    .putString("step_goal", goalText)
                    .putString("sos_phone", phoneText)
                    .apply();

            // Sprawdź czy obecne kroki < nowy cel → restart serwisu
            int currentSteps = sharedPreferences.getInt("last_recorded_steps", 0);
            int newGoal = Integer.parseInt(goalText);
            if (currentSteps < newGoal) {
                // Cel nie osiągnięty z nowym limitem - odblokuj i restart serwis
                sharedPreferences.edit().putBoolean("goal_reached_today", false).apply();
                Intent serviceIntent = new Intent(requireContext(), StepCounterService.class);
                androidx.core.content.ContextCompat.startForegroundService(requireContext(), serviceIntent);
            }

            Toast.makeText(getContext(), "Zapisano ustawienia", Toast.LENGTH_SHORT).show();
        });

        // --- Logika z brancha test (Prawdziwe wylogowanie z Firebase) ---
        Button logoutButton = view.findViewById(R.id.settingsLogoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
