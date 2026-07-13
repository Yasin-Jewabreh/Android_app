package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final Button loginButton = findViewById(R.id.loginButton);
        final EditText emailField = findViewById(R.id.emailText);
        final EditText passwortField = findViewById(R.id.passwortText);




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
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
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
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
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