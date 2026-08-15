package com.example.android_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Firebase und gespeicherte Login-Einstellungen
    private FirebaseAuth mAuth;
    private SharedPreferences loginPreferences;
    private static final String PREF_NAME = "login_preferences";
    private static final String KEY_REMEMBER_LOGIN = "remember_login";
    private CheckBox checkBoxRememberLogin;
    private boolean rememberLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Views initialisieren
        final Button loginButton = findViewById(R.id.btnAnmelden);
        final EditText emailField = findViewById(R.id.etAnmeldungEmail);
        final EditText passwortField = findViewById(R.id.etAnmeldungPasswort);
        final TextView registerText = findViewById(R.id.tvZurRegistrierung);
        checkBoxRememberLogin = findViewById(R.id.cbAnmeldungMerken);

        // SharedPreferences und Firebase initialisieren
        loginPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        // Gespeicherten Login-Status laden
        rememberLogin = loginPreferences.getBoolean(KEY_REMEMBER_LOGIN, false);
        checkBoxRememberLogin.setChecked(rememberLogin);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Prüfen, ob der Benutzer automatisch eingeloggt werden soll
        if (rememberLogin && currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        } else if (!rememberLogin && currentUser != null) {
            mAuth.signOut();
        }

        // Login durchführen
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCredentials(emailField, passwortField);
            }
        });

        // Zur Registrierung wechseln
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Eingaben prüfen und Benutzer über Firebase anmelden
    private void checkCredentials(EditText emailField, EditText passwordField) {
        String email = emailField.getText().toString().trim();
        String passwort = passwordField.getText().toString();

        // E-Mail-Adresse prüfen
        if (email.isEmpty()) {
            emailField.setError("Bitte geben Sie eine gültige Emailadresse!");
            emailField.requestFocus();
            return;
        }

        // Passwort prüfen
        if (passwort.isEmpty()) {
            passwordField.setError("Bitte füllen Sie dieses Feld aus!");
            passwordField.requestFocus();
            return;
        }

        // Anmeldung bei Firebase
        mAuth.signInWithEmailAndPassword(email, passwort).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                // Einstellung zum automatischen Login speichern
                rememberLogin = checkBoxRememberLogin.isChecked();
                loginPreferences.edit().putBoolean(KEY_REMEMBER_LOGIN, rememberLogin).apply();

                // Nach erfolgreichem Login zum Dashboard wechseln
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // Fehlermeldung bei nicht erfolgreicher Anmeldung
                Toast.makeText(LoginActivity.this, "Anmeldung fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                passwordField.setText("");
            }
        });
    }
}