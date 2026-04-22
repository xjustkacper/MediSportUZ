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
/**
 * @brief Aktywność odpowiedzialna za proces logowania użytkownika do aplikacji.
 * * Klasa ta integruje uwierzytelnianie Firebase (FirebaseAuth) za pomocą adresu e-mail
 * i hasła. Zawiera również wbudowany, lokalny mechanizm CAPTCHA zapobiegający
 * zautomatyzowanym próbom logowania, a także obsługuje proces odzyskiwania hasła.
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * @brief Główny obiekt dostępu do usług uwierzytelniania Firebase.
     */
    private FirebaseAuth mAuth;
    private TextInputEditText emailInput, passwordInput, captchaInput;
    private TextView captchaText;
    /**
     * @brief Przechowuje aktualnie wygenerowany, prawidłowy ciąg znaków CAPTCHA.
     */
    private String currentCaptcha;
    /**
     * @brief Inicjalizuje aktywność, widoki oraz ustawia nasłuchiwacze zdarzeń.
     * * Metoda ta wywoływana jest przy tworzeniu aktywności. Konfiguruje Firebase,
     * przypisuje widoki do zmiennych, generuje pierwszą zagadkę CAPTCHA oraz
     * podpina akcje pod przyciski logowania, rejestracji i resetowania hasła.
     * * @param savedInstanceState Stan zapisany z poprzedniej instancji aktywności (jeśli istnieje).
     */
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
    /**
     * @brief Sprawdza status sesji użytkownika przy każdym uruchomieniu aktywności.
     * * Jeśli użytkownik jest już zalogowany i jego adres e-mail został zweryfikowany,
     * następuje automatyczne przekierowanie do głównego ekranu aplikacji (MainActivity),
     * pomijając ekran logowania.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            goToMain();
        }
    }
    /**
     * @brief Generuje nowy, 5-znakowy kod CAPTCHA i aktualizuje interfejs.
     * * Wykorzystuje zdefiniowany zbiór znaków, celowo pomijając znaki łatwe do
     * pomylenia (np. '0', 'O', '1', 'I', 'l'). Wygenerowany ciąg jest wyświetlany
     * na ekranie i przypisywany do zmiennej currentCaptcha w celu późniejszej weryfikacji.
     */
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
    /**
     * @brief Przeprowadza walidację formularza i próbuje zalogować użytkownika.
     * * Proces obejmuje:
     * 1. Sprawdzenie, czy żadne z pól (e-mail, hasło, CAPTCHA) nie jest puste.
     * 2. Weryfikację poprawności wpisanego kodu CAPTCHA.
     * 3. Wysłanie zapytania logowania do Firebase Auth.
     * 4. Sprawdzenie, czy adres e-mail przypisany do konta został pomyślnie zweryfikowany.
     * W przypadku niepowodzenia na którymkolwiek etapie, generowany jest nowy kod CAPTCHA.
     */
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
    /**
     * @brief Wyświetla okno dialogowe umożliwiające reset hasła.
     * * Tworzy dynamiczny AlertDialog z polem tekstowym na adres e-mail.
     * Jeśli w głównym oknie logowania wpisano już e-mail, pole to zostaje
     * nim automatycznie wypełnione. Po zatwierdzeniu wysyła link resetujący
     * na podany adres za pośrednictwem Firebase.
     */
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
    /**
     * @brief Przenosi użytkownika do głównego ekranu aplikacji i kończy aktualną aktywność.
     * * Używane po pomyślnym zalogowaniu (lub gdy użytkownik był już zalogowany),
     * zapobiegając możliwości powrotu do ekranu logowania za pomocą przycisku "Wstecz".
     */
    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
