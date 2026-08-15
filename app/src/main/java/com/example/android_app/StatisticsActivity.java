package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views initialisieren
        TextView tvAnzahlGesamt = findViewById(R.id.tvAnzahlGesamt);
        TextView tvAnzahlOffen = findViewById(R.id.tvAnzahlOffen);
        TextView tvAnzahlBewertet = findViewById(R.id.tvAnzahlBewertet);
        TextView tvDurchschnittGesamt = findViewById(R.id.tvDurchschnittGesamt);
        TextView tvDurchschnittStimmung = findViewById(R.id.tvDurchschnittStimmung);
        TextView tvDurchschnittKategorie = findViewById(R.id.tvDurchschnittKategorie);
        TextView tvBesteEntscheidungen = findViewById(R.id.tvBesteEntscheidungen);
        TextView tvSchlechtesteEntscheidungen = findViewById(R.id.tvSchlechtesteEntscheidungen);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Firebase initialisieren und aktuellen Benutzer prüfen
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(StatisticsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Datenbankreferenz für die Entscheidungen des aktuellen Benutzers erstellen
        String currentUserId = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User").child(currentUserId).child("Decisions");

        // Entscheidungen aus Firebase laden und Statistiken berechnen
        ref.get().addOnSuccessListener(snapshot -> {
            int anzahlGesamt = 0;
            int anzahlBewertet = 0;
            int anzahlOffen = 0;
            float summeBewertungen = 0;

            // Summen und Anzahl der Bewertungen nach Stimmung
            HashMap<String, Float> summeNachStimmung = new HashMap<>();
            HashMap<String, Integer> anzahlNachStimmung = new HashMap<>();

            // Summen und Anzahl der Bewertungen nach Kategorie
            HashMap<String, Float> summeNachKategorie = new HashMap<>();
            HashMap<String, Integer> anzahlNachKategorie = new HashMap<>();

            // Bewertete Entscheidungen für die Besten- und Schlechtestenliste
            List<Decision> bewerteteEntscheidungen = new ArrayList<>();

            // Alle Entscheidungen durchgehen
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Decision decision = dataSnapshot.getValue(Decision.class);

                if (decision == null) {
                    continue;
                }

                anzahlGesamt++;

                if (decision.isIstBewertet()) {
                    anzahlBewertet++;

                    float aktuelleBewertung = decision.getBewertung();
                    summeBewertungen += aktuelleBewertung;
                    bewerteteEntscheidungen.add(decision);

                    // Bewertung nach Stimmung erfassen
                    String stimmung = decision.getStimmung();

                    if (stimmung != null && !stimmung.isEmpty()) {
                        float bisherigeStimmungssumme = summeNachStimmung.getOrDefault(stimmung, 0f);
                        int bisherigeStimmungszahl = anzahlNachStimmung.getOrDefault(stimmung, 0);

                        summeNachStimmung.put(stimmung, bisherigeStimmungssumme + aktuelleBewertung);
                        anzahlNachStimmung.put(stimmung, bisherigeStimmungszahl + 1);
                    }

                    // Bewertung nach Kategorie erfassen
                    String kategorie = decision.getKategorie();

                    if (kategorie != null && !kategorie.isEmpty()) {
                        float bisherigeKategorieSumme = summeNachKategorie.getOrDefault(kategorie, 0f);
                        int bisherigeKategoriezahl = anzahlNachKategorie.getOrDefault(kategorie, 0);

                        summeNachKategorie.put(kategorie, bisherigeKategorieSumme + aktuelleBewertung);
                        anzahlNachKategorie.put(kategorie, bisherigeKategoriezahl + 1);
                    }
                } else {
                    anzahlOffen++;
                }
            }

            // Anzahl der Entscheidungen anzeigen
            tvAnzahlGesamt.setText(String.valueOf(anzahlGesamt));
            tvAnzahlOffen.setText(String.valueOf(anzahlOffen));
            tvAnzahlBewertet.setText(String.valueOf(anzahlBewertet));

            // Statistiken für bewertete Entscheidungen anzeigen
            if (anzahlBewertet > 0) {

                // Gesamtdurchschnitt berechnen
                float durchschnittGesamt = summeBewertungen / anzahlBewertet;
                tvDurchschnittGesamt.setText(String.format(Locale.getDefault(), "%.1f von 10", durchschnittGesamt));

                // Durchschnitt nach Stimmung berechnen
                StringBuilder stimmungsText = new StringBuilder();

                for (String stimmung : summeNachStimmung.keySet()) {
                    float summe = summeNachStimmung.get(stimmung);
                    int anzahl = anzahlNachStimmung.get(stimmung);
                    float durchschnitt = summe / anzahl;
                    stimmungsText.append(stimmung).append(": ").append(String.format(Locale.getDefault(), "%.1f", durchschnitt)).append(" von 10 bei ").append(anzahl).append(anzahl == 1 ? " Bewertung\n" : " Bewertungen\n");
                }

                tvDurchschnittStimmung.setText(stimmungsText.toString().trim());

                // Durchschnitt nach Kategorie berechnen
                StringBuilder kategorieText = new StringBuilder();

                for (String kategorie : summeNachKategorie.keySet()) {
                    float summe = summeNachKategorie.get(kategorie);
                    int anzahl = anzahlNachKategorie.get(kategorie);
                    float durchschnitt = summe / anzahl;
                    kategorieText.append(kategorie).append(": ").append(String.format(Locale.getDefault(), "%.1f", durchschnitt)).append(" von 10 bei ").append(anzahl).append(anzahl == 1 ? " Bewertung\n" : " Bewertungen\n");
                }

                tvDurchschnittKategorie.setText(kategorieText.toString().trim());

                // Bewertete Entscheidungen nach Bewertung sortieren
                bewerteteEntscheidungen.sort((entscheidung1, entscheidung2) -> Float.compare(entscheidung2.getBewertung(), entscheidung1.getBewertung()));

                // Die drei besten Entscheidungen anzeigen
                StringBuilder besteText = new StringBuilder();
                int anzahlAnzeigen = Math.min(3, bewerteteEntscheidungen.size());

                for (int i = 0; i < anzahlAnzeigen; i++) {
                    Decision entscheidung = bewerteteEntscheidungen.get(i);
                    besteText.append(i + 1).append(". ").append(entscheidung.getTitel()).append(" - ").append(entscheidung.getBewertung()).append(" von 10\n");
                }

                tvBesteEntscheidungen.setText(besteText.toString().trim());

                // Die drei schlechtesten Entscheidungen anzeigen
                StringBuilder schlechtesteText = new StringBuilder();

                for (int i = 0; i < anzahlAnzeigen; i++) {
                    int index = bewerteteEntscheidungen.size() - 1 - i;
                    Decision entscheidung = bewerteteEntscheidungen.get(index);
                    schlechtesteText.append(i + 1).append(". ").append(entscheidung.getTitel()).append(" - ").append(entscheidung.getBewertung()).append(" von 10\n");
                }

                tvSchlechtesteEntscheidungen.setText(schlechtesteText.toString().trim());

                // Hinweis anzeigen, wenn noch keine Entscheidungen bewertet wurden
            } else {
                tvDurchschnittGesamt.setText("Du hast noch keine bewerteten Entscheidungen!");
                tvDurchschnittStimmung.setText("Du hast noch keine bewerteten Entscheidungen!");
                tvDurchschnittKategorie.setText("Du hast noch keine bewerteten Entscheidungen!");
                tvBesteEntscheidungen.setText("Du hast noch keine bewerteten Entscheidungen!");
                tvSchlechtesteEntscheidungen.setText("Du hast noch keine bewerteten Entscheidungen!");
            }

            // Fehler beim Laden der Entscheidungen
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StatisticsActivity.this, "Statistiken konnten nicht geladen werden: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Statistik als aktuellen Punkt der Bottom Navigation markieren
        bottomNav.setSelectedItemId(R.id.nav_statistics);

        // Navigation zwischen den Hauptbereichen der App
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(StatisticsActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(StatisticsActivity.this, AllDecisionsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_statistics) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(StatisticsActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }
}