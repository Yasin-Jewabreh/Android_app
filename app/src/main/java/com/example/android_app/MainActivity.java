package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final Button loginButton = findViewById(R.id.loginButton);




        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {

                if (checkCredentials()) {
                    Toast.makeText(view.getContext(), "Login erfolgreich!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        final TextView registerText = findViewById(R.id.loginText);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    public boolean checkCredentials() {
        boolean status = false;
        EditText emailField = findViewById(R.id.emailText);
        EditText passwordField = findViewById(R.id.passwortText);
        String emailText = emailField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();

        if (emailText.isEmpty()) {
            emailField.setError("Bitte füllen Sie dieses Feld aus!");
        }
        if (passwordText.isEmpty()) {
            passwordField.setError("Bitte füllen Sie dieses Feld aus!");
        }
        if (!emailText.isEmpty() && !passwordText.isEmpty()) {
            status = true;
        }
        return status;
    }
}