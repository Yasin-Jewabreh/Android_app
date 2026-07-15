package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference UserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        UserRef = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User");
        mAuth = FirebaseAuth.getInstance();

        final EditText emailText = findViewById(R.id.emailTextRegister);
        final EditText passwortText = findViewById(R.id.passwortTextRegister);
        final EditText nameText = findViewById(R.id.decisionTitel);
        final Button registerButton = findViewById(R.id.registerButton);
        final EditText passwortTextWiederholen = findViewById(R.id.passwortTextRegisterRepeat);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString().trim();
                String email = emailText.getText().toString().trim();
                String passwort = passwortText.getText().toString();
                String passwortwiederholen = passwortTextWiederholen.getText().toString();

                if (email.isEmpty() || name.isEmpty() || passwort.isEmpty()) {
                    Toast.makeText(v.getContext(), "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!passwort.equals(passwortwiederholen)) {
                    passwortTextWiederholen.setError("Die Passwörter müssen übereinstimmen!");
                }


                mAuth.createUserWithEmailAndPassword(email, passwort)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();

                                User user = new User(userId, name, email, passwort);
                                UserRef.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(v.getContext(), "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(v.getContext(), "Datenbank-Speichern fehlgeschlagen!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                String fehlerMeldung = task.getException() != null ? task.getException().getMessage() : "Unbekannter Fehler";
                                Toast.makeText(v.getContext(), "Fehler: " + fehlerMeldung, Toast.LENGTH_LONG).show();

                            }
                        });

            }
        });

        final TextView loginText = findViewById(R.id.loginText);

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}