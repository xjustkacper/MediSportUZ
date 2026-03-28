package com.example.medisportuz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText nameInput, emailInput, passwordInput, repeatPasswordInput, captchaInput;
    private TextView captchaText;
    private String currentCaptcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        nameInput = findViewById(R.id.registerNameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        repeatPasswordInput = findViewById(R.id.registerRepeatPasswordInput);
        captchaInput = findViewById(R.id.captchaInput);
        captchaText = findViewById(R.id.captchaText);
        TextView captchaRefresh = findViewById(R.id.captchaRefresh);
        Button registerButton = findViewById(R.id.registerButton);
        TextView backLink = findViewById(R.id.registerBackLink);
        TextView passwordError = findViewById(R.id.registerPasswordError);

        // Hide the static password error hint initially
        passwordError.setVisibility(android.view.View.GONE);

        // Generate initial captcha
        generateCaptcha();

        // Refresh captcha on click
        captchaRefresh.setOnClickListener(v -> generateCaptcha());

        // Register button
        registerButton.setOnClickListener(v -> registerUser());

        // Back link — go back to login
        backLink.setOnClickListener(v -> finish());
    }

    private void generateCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        currentCaptcha = sb.toString();
        captchaText.setText(currentCaptcha);

        // Clear captcha input field
        if (captchaInput != null) {
            captchaInput.setText("");
        }
    }

    private void registerUser() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        String repeatPassword = repeatPasswordInput.getText() != null ? repeatPasswordInput.getText().toString().trim() : "";
        String captchaValue = captchaInput.getText() != null ? captchaInput.getText().toString().trim() : "";

        // Validate empty fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)
                || TextUtils.isEmpty(captchaValue)) {
            Toast.makeText(this, R.string.auth_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            Toast.makeText(this, R.string.auth_password_short, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate passwords match
        if (!password.equals(repeatPassword)) {
            Toast.makeText(this, R.string.auth_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate captcha
        if (!captchaValue.equals(currentCaptcha)) {
            Toast.makeText(this, R.string.auth_captcha_wrong, Toast.LENGTH_LONG).show();
            generateCaptcha();
            return;
        }

        // All validations passed — register with Firebase
        Toast.makeText(this, R.string.auth_registering, Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 1. Zapis Imienia na obiekcie Auth (tak jak było)
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdate);

                            // 2. Utworzenie obiektu dla Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("imię", name);
                            userMap.put("email", email);
                            userMap.put("utworzono", System.currentTimeMillis());

                            // 3. Zapis do kolekcji "users" w bazie Firestore pod kluczem UID
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(user.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Sukces zapisu do bazy — wyślij maila i wyloguj
                                        sendVerificationEmail();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Jeśli zapis się nie powiedzie, mimo wszystko wyślij maila
                                        sendVerificationEmail();
                                    });
                        } else {
                            sendVerificationEmail();
                        }
                    } else {
                        // Handle specific Firebase errors
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this,
                                    R.string.auth_email_exists, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    R.string.auth_register_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    R.string.auth_verify_email_sent, Toast.LENGTH_LONG).show();
                        }
                        // Sign out — user must verify email before logging in
                        mAuth.signOut();
                        // Go back to login screen
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
        }
    }

    private void goToMain() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }
}
