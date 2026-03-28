package com.example.medisportuz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText emailInput, passwordInput, captchaInput;
    private TextView captchaText;
    private String currentCaptcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        captchaInput = findViewById(R.id.loginCaptchaInput);
        captchaText = findViewById(R.id.loginCaptchaText);
        TextView captchaRefresh = findViewById(R.id.loginCaptchaRefresh);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.loginRegisterLink);
        TextView forgotPassword = findViewById(R.id.loginForgotPassword);

        // Generate initial captcha
        generateCaptcha();

        // Refresh captcha on click
        captchaRefresh.setOnClickListener(v -> generateCaptcha());

        loginButton.setOnClickListener(v -> loginUser());

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        forgotPassword.setOnClickListener(v -> showResetPasswordDialog());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            goToMain();
        }
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

        if (captchaInput != null) {
            captchaInput.setText("");
        }
    }

    private void loginUser() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        String captchaValue = captchaInput.getText() != null ? captchaInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(captchaValue)) {
            Toast.makeText(this, R.string.auth_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!captchaValue.equals(currentCaptcha)) {
            Toast.makeText(this, R.string.auth_captcha_wrong, Toast.LENGTH_LONG).show();
            generateCaptcha();
            return;
        }

        Toast.makeText(this, R.string.auth_logging_in, Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            goToMain();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    R.string.auth_email_not_verified, Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                R.string.auth_login_failed, Toast.LENGTH_LONG).show();
                    }
                    generateCaptcha();
                });
    }

    private void showResetPasswordDialog() {
        EditText emailField = new EditText(this);
        emailField.setHint(R.string.login_email_hint);
        emailField.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailField.setPadding(64, 32, 64, 16);

        // Pre-fill with email from login field if available
        if (emailInput.getText() != null && !TextUtils.isEmpty(emailInput.getText().toString().trim())) {
            emailField.setText(emailInput.getText().toString().trim());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.auth_reset_title)
                .setMessage(R.string.auth_reset_message)
                .setView(emailField)
                .setPositiveButton(R.string.auth_reset_send, (dialog, which) -> {
                    String email = emailField.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(this, R.string.auth_empty_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this,
                                            R.string.auth_reset_sent, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            R.string.auth_reset_failed, Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton(R.string.auth_reset_cancel, null)
                .show();
    }

    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
