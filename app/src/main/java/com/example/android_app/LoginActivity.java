package com.example.android_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.*;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

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

        final Button loginButton = findViewById(R.id.loginButton);
        final EditText emailField = findViewById(R.id.emailText);
        final EditText passwortField = findViewById(R.id.passwortText);
        checkBoxRememberLogin = findViewById(R.id.checkLoginSpeichern);

        loginPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        rememberLogin = loginPreferences.getBoolean(KEY_REMEMBER_LOGIN, false);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = mAuth.getCurrentUser().getUid();
        }
        checkBoxRememberLogin.setChecked(rememberLogin);

        if (rememberLogin && currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                checkCredentials(emailField, passwortField);
            }
        });

        final TextView registerText = findViewById(R.id.loginText);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void checkCredentials(EditText emailField, EditText passwordField) {
        String email = emailField.getText().toString().trim();
        String passwort = passwordField.getText().toString();

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, passwort)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        rememberLogin = checkBoxRememberLogin.isChecked();
                        loginPreferences.edit().putBoolean(KEY_REMEMBER_LOGIN, rememberLogin).apply();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        passwordField.setError("Passwort oder Email ist falsch!");
                        passwordField.setText("");

                    }
                });

        if (email.isEmpty()) {
            emailField.setError("Bitte geben Sie eine gültige Emailadresse!");
        }
        if (passwort.isEmpty()) {
            passwordField.setError("Bitte füllen Sie dieses Feld aus!");
        }
    }
}