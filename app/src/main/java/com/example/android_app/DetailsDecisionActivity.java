package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetailsDecisionActivity extends AppCompatActivity {

    // Views
    TextView tvTitel, tvKategorie, tvStimmung, tvNotizen, tvEntscheidung, tvErinnerungAm, tvOptionen, tvBewertung;
    Button btnBewerten, btnBearbeiten;
    MaterialToolbar topAppBar;

    // Optionen der Entscheidung
    List<String> meineOptionen = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_decision);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views initialisieren
        tvTitel = findViewById(R.id.tvCardTitel);
        tvKategorie = findViewById(R.id.tvCardKategorie);
        tvStimmung = findViewById(R.id.tvCardStimmung);
        tvNotizen = findViewById(R.id.tvCardNotizen);
        tvErinnerungAm = findViewById(R.id.tvCardErinnerungAm);
        tvEntscheidung = findViewById(R.id.tvCardEntscheidung);
        tvOptionen = findViewById(R.id.tvCardOptionen);
        tvBewertung = findViewById(R.id.tvCardBewertung);
        btnBewerten = findViewById(R.id.btnBewerten);
        btnBearbeiten = findViewById(R.id.btnBearbeiten);
        topAppBar = findViewById(R.id.topAppBar);

        // Zurückbutton
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Firebase initialisieren und aktuellen Benutzer prüfen
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(DetailsDecisionActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String currentUserId = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User").child(currentUserId).child("Decisions");

        // ID der ausgewählten Entscheidung aus dem Intent holen
        String key = getIntent().getStringExtra("key");

        if (key == null) {
            Toast.makeText(DetailsDecisionActivity.this, "Entscheidung konnte nicht gefunden werden!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Zur Bewertung der Entscheidung wechseln
        btnBewerten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsDecisionActivity.this, RateDecisionActivity.class);
                intent.putExtra("key", key);
                startActivity(intent);
            }
        });

        // Entscheidung zum Bearbeiten öffnen
        btnBearbeiten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsDecisionActivity.this, AddDecisionActivity.class);
                intent.putExtra("key", key);
                intent.putExtra("bearbeiten", true);
                startActivity(intent);
            }
        });

        // Entscheidung aus Firebase laden und bei Änderungen aktualisieren
        ref.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Prüfen, ob die Entscheidung existiert
                if (!snapshot.exists()) {
                    Toast.makeText(DetailsDecisionActivity.this, "Entscheidung konnte nicht gefunden werden!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Firebase-Daten in ein Decision-Objekt laden
                Decision decision = snapshot.getValue(Decision.class);

                if (decision == null) {
                    Toast.makeText(DetailsDecisionActivity.this, "Entscheidung konnte nicht geladen werden!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Grunddaten der Entscheidung anzeigen
                tvTitel.setText(decision.getTitel());
                tvKategorie.setText("Kategorie: " + decision.getKategorie());
                tvStimmung.setText("Deine Stimmung: " + decision.getStimmung());
                tvEntscheidung.setText("Entscheidung: " + decision.getEntscheidung());

                // Optionale Notizen anzeigen
                if (decision.getBeschreibung() != null && !decision.getBeschreibung().isEmpty()) {
                    tvNotizen.setText("Notizen: " + decision.getBeschreibung());
                } else {
                    tvNotizen.setText("Notizen: /");
                }

                // Optionen laden und als Text zusammensetzen
                meineOptionen.clear();

                if (decision.getOptionen() != null) {
                    meineOptionen.addAll(decision.getOptionen());
                }

                StringBuilder optionenText = new StringBuilder();

                for (String optionText : meineOptionen) {
                    optionenText.append("- ").append(optionText).append("\n");
                }

                tvOptionen.setText(optionenText.toString());

                // Bewertungsstatus anzeigen und Bearbeiten-Button entsprechend steuern
                if (decision.isIstBewertet()) {
                    tvBewertung.setText("Bewertung: " + decision.getBewertung() + " von 10");
                    btnBewerten.setText("Bewertung ändern");
                    btnBearbeiten.setEnabled(false);
                    btnBearbeiten.setAlpha(0.5f);
                } else {
                    tvBewertung.setText("");
                    btnBewerten.setText("Bewerten");
                    btnBearbeiten.setEnabled(true);
                    btnBearbeiten.setAlpha(1.0f);
                }

                // Erinnerungsdatum formatieren und anzeigen
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                String formatiertesDatum = sdf.format(new java.util.Date(decision.getErinnerungAm()));
                tvErinnerungAm.setText("Am " + formatiertesDatum + " bewerten");
            }

            // Fehler beim Laden aus Firebase
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailsDecisionActivity.this, "Entscheidung konnte nicht geladen werden: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}