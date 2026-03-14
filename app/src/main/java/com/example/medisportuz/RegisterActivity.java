package com.example.medisportuz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button registerButton = findViewById(R.id.registerButton);
        TextView backLink = findViewById(R.id.registerBackLink);

        // Register button — go to main screen (no validation, mockup only)
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        // Back link — go back to login
        backLink.setOnClickListener(v -> {
            finish();
        });
    }
}
