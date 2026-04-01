package com.example.medisportuz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class SettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private TextInputEditText stepGoalInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        stepGoalInput = view.findViewById(R.id.settingsStepGoalInput);

        // Load current goal
        String currentGoal = sharedPreferences.getString("step_goal", "10000");
        stepGoalInput.setText(currentGoal);

        // Save goal on change
        stepGoalInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedPreferences.edit().putString("step_goal", s.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        Button logoutButton = view.findViewById(R.id.settingsLogoutButton);
        logoutButton.setOnClickListener(v -> {
            // Mock logout
            requireActivity().finish();
        });

        return view;
    }
}
