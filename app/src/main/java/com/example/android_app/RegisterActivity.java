package com.example.android_app;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Views initialisieren
        final EditText emailText = findViewById(R.id.etRegistrierungEmail);
        final EditText passwortText = findViewById(R.id.etRegistrierungPasswort);
        final EditText nameText = findViewById(R.id.etNutzername);
        final Button registerButton = findViewById(R.id.btnRegistrieren);
        final EditText passwortTextWiederholen = findViewById(R.id.etRegistrierungPasswortWiederholen);
        final TextView loginText = findViewById(R.id.tvZurAnmeldung);

        // Firebase initialisieren
        userRef = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User");
        mAuth = FirebaseAuth.getInstance();

        // Registrierung durchführen
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString().trim();
                String email = emailText.getText().toString().trim();
                String passwort = passwortText.getText().toString();
                String passwortwiederholen = passwortTextWiederholen.getText().toString();

                // Prüfen, ob alle Felder ausgefüllt wurden
                if (email.isEmpty() || name.isEmpty() || passwort.isEmpty() || passwortwiederholen.isEmpty()) {
                    Toast.makeText(v.getContext(), "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prüfen, ob beide Passwörter übereinstimmen
                if (!passwort.equals(passwortwiederholen)) {
                    passwortTextWiederholen.setError("Die Passwörter müssen übereinstimmen!");
                    passwortTextWiederholen.requestFocus();
                    return;
                }

                // Benutzer bei Firebase Authentication registrieren
                mAuth.createUserWithEmailAndPassword(email, passwort).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Prüfen, ob der registrierte Benutzer verfügbar ist
                        if (mAuth.getCurrentUser() == null) {
                            Toast.makeText(RegisterActivity.this, "Benutzer konnte nicht geladen werden!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userId = mAuth.getCurrentUser().getUid();

                        // Benutzerdaten ohne Passwort in der Realtime Database speichern
                        User user = new User(userId, name, email);

                        userRef.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                // Benutzer abmelden und zur Login-Seite zurückkehren
                                Toast.makeText(v.getContext(), "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(v.getContext(), "Datenbank-Speichern fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        String fehlerMeldung = task.getException() != null ? task.getException().getMessage() : "Unbekannter Fehler";
                        Toast.makeText(v.getContext(), "Fehler: " + fehlerMeldung, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Zur Login-Seite zurückkehren
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Systemleisten berücksichtigen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}