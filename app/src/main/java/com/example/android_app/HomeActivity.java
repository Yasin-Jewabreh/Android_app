package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Views initialisieren
        Button btnNeueEntscheidung = findViewById(R.id.btnNeueEntscheidung);
        TextView tvDashboardGesamt = findViewById(R.id.tvDashboardGesamt);
        TextView tvDashboardOffen = findViewById(R.id.tvDashboardOffen);
        TextView tvDashboardBewertet = findViewById(R.id.tvDashboardBewertet);
        TextView tvFaelligeBewertungen = findViewById(R.id.tvFaelligeBewertungen);
        TextView tvLetzteEntscheidungen = findViewById(R.id.tvLetzteEntscheidungen);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Firebase Authentication initialisieren und aktuellen Benutzer prüfen
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Datenbankreferenz für die Entscheidungen des aktuellen Benutzers erstellen
        String currentUserId = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User").child(currentUserId).child("Decisions");

        // Neue Entscheidung erstellen
        btnNeueEntscheidung.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddDecisionActivity.class);
            startActivity(intent);
        });

        // Entscheidungen aus Firebase laden und Dashboard aktualisieren
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Zähler und Liste für die Dashboard-Auswertung vorbereiten
                int anzahlGesamt = 0;
                int anzahlOffen = 0;
                int anzahlBewertet = 0;
                int anzahlFaellig = 0;

                List<Decision> entscheidungen = new ArrayList<>();
                long aktuelleZeit = System.currentTimeMillis();

                // Alle Entscheidungen auswerten
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Decision decision = dataSnapshot.getValue(Decision.class);

                    if (decision == null) {
                        continue;
                    }

                    anzahlGesamt++;
                    entscheidungen.add(decision);

                    if (decision.isIstBewertet()) {
                        anzahlBewertet++;
                    } else {
                        anzahlOffen++;

                        if (decision.getErinnerungAm() <= aktuelleZeit) {
                            anzahlFaellig++;
                        }
                    }
                }

                // Anzahl der Entscheidungen im Dashboard anzeigen
                tvDashboardGesamt.setText(String.valueOf(anzahlGesamt));
                tvDashboardOffen.setText(String.valueOf(anzahlOffen));
                tvDashboardBewertet.setText(String.valueOf(anzahlBewertet));

                // Fällige Bewertungen anzeigen
                if (anzahlFaellig == 0) {
                    tvFaelligeBewertungen.setText("Aktuell ist keine Bewertung fällig.");
                } else if (anzahlFaellig == 1) {
                    tvFaelligeBewertungen.setText("1 Entscheidung wartet auf deine Bewertung.");
                } else {
                    tvFaelligeBewertungen.setText(anzahlFaellig + " Entscheidungen warten auf deine Bewertung.");
                }

                // Entscheidungen nach Erstellungsdatum sortieren
                entscheidungen.sort((entscheidung1, entscheidung2) -> Long.compare(entscheidung2.getErstellAm(), entscheidung1.getErstellAm()));

                // Die letzten drei Entscheidungen anzeigen
                if (entscheidungen.isEmpty()) {
                    tvLetzteEntscheidungen.setText("Du hast noch keine Entscheidung erstellt.");
                } else {
                    StringBuilder letzteText = new StringBuilder();
                    int anzahlAnzeigen = Math.min(3, entscheidungen.size());

                    for (int i = 0; i < anzahlAnzeigen; i++) {
                        Decision decision = entscheidungen.get(i);
                        letzteText.append("• ").append(decision.getTitel());

                        if (i < anzahlAnzeigen - 1) {
                            letzteText.append("\n");
                        }
                    }

                    tvLetzteEntscheidungen.setText(letzteText.toString());
                }
            }

            // Fehler beim Laden der Dashboard-Daten
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Dashboard konnte nicht geladen werden.", Toast.LENGTH_SHORT).show();
            }
        });

        // Dashboard als aktuellen Punkt der Bottom Navigation markieren
        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        // Navigation zwischen den Hauptbereichen der App
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(HomeActivity.this, AllDecisionsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_statistics) {
                startActivity(new Intent(HomeActivity.this, StatisticsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }
}