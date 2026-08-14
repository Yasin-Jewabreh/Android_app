package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    String currentUserId;
    private TextView tvAnzahlGesamt, tvAnzahlOffen, tvAnzahlBewertet,
            tvDurchschnittGesamt, tvDurchschnittStimmung, tvDurchschnittKategorie,
            tvBesteEntscheidungen, tvSchlechtesteEntscheidungen;

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


        tvAnzahlGesamt = findViewById(R.id.tvAnzahlGesamt);
        tvAnzahlOffen = findViewById(R.id.tvAnzahlOffen);
        tvAnzahlBewertet = findViewById(R.id.tvAnzahlBewertet);
        tvDurchschnittGesamt = findViewById(R.id.tvDurchschnittGesamt);
        tvDurchschnittStimmung = findViewById(R.id.tvDurchschnittStimmung);
        tvDurchschnittKategorie = findViewById(R.id.tvDurchschnittKategorie);
        tvBesteEntscheidungen = findViewById(R.id.tvBesteEntscheidungen);
        tvSchlechtesteEntscheidungen = findViewById(R.id.tvSchlechtesteEntscheidungen);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User").child(currentUserId).child("Decisions");

        ref.get().addOnSuccessListener(snapshot -> {
            int anzahlGesamt = 0;
            int anzahlBewertet = 0;
            int anzahlOffen = 0;

            float summeBewertungen = 0;

            HashMap<String, Float> summeNachStimmung = new HashMap<>();
            HashMap<String, Integer> anzahlNachStimmung = new HashMap<>();

            HashMap<String, Float> summeNachKategorie = new HashMap<>();
            HashMap<String, Integer> anzahlNachKategorie = new HashMap();

            List<Decision> bewerteteEntscheidungen = new ArrayList<>();

            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Decision decision = dataSnapshot.getValue(Decision.class);

                if (decision == null) {
                    continue;
                }

                anzahlGesamt++;
                float aktuelleBewertung = 0;

                if (decision.isIstBewertet()) {
                    anzahlBewertet++;

                    aktuelleBewertung = decision.getBewertung();
                    summeBewertungen += aktuelleBewertung;
                    bewerteteEntscheidungen.add(decision);


                    String stimmung = decision.getStimmung();
                    float bisherigeStimmungssumme = summeNachStimmung.getOrDefault(stimmung, 0f);
                    int bisherigeStimmungszahl = anzahlNachStimmung.getOrDefault(stimmung, 0);

                    summeNachStimmung.put(stimmung, bisherigeStimmungssumme + aktuelleBewertung);
                    anzahlNachStimmung.put(stimmung, bisherigeStimmungszahl + 1);

                    String kategorie = decision.getKategorie();
                    float bisherigeKategorieSumme = summeNachKategorie.getOrDefault(kategorie, 0f);
                    int bisherigeKategoriezahl = anzahlNachKategorie.getOrDefault(kategorie, 0);

                    summeNachKategorie.put(kategorie, bisherigeKategorieSumme + aktuelleBewertung);
                    anzahlNachKategorie.put(kategorie, bisherigeKategoriezahl + 1);
                } else {
                    anzahlOffen++;
                }
            }

            tvAnzahlGesamt.setText(String.valueOf(anzahlGesamt));
            tvAnzahlOffen.setText(String.valueOf(anzahlOffen));
            tvAnzahlBewertet.setText(String.valueOf(anzahlBewertet));

                if (anzahlBewertet>0) {
                    tvDurchschnittGesamt.setText(String.valueOf(summeBewertungen/ anzahlBewertet) + " von 10");
                    List<String> stimmungen = new ArrayList<>(summeNachStimmung.keySet());
                    StringBuilder stimmungsText = new StringBuilder();
                    for (String stimmung : stimmungen){
                        float summe = summeNachStimmung.get(stimmung);
                        int anzahl =  anzahlNachStimmung.get(stimmung);
                        float durchschnitt = summe/anzahl;
                        stimmungsText.append(stimmung + ": " +durchschnitt + " von 10 bei " + anzahl +" Bewertung/en\n");
                    }
                    tvDurchschnittStimmung.setText(stimmungsText.toString().trim());

                    List<String> kategorien = new ArrayList<>(summeNachKategorie.keySet());
                    StringBuilder kategorieText = new StringBuilder();
                    for (String kategorie : kategorien){
                        float summe = summeNachKategorie.get(kategorie);
                        int anzahl =  anzahlNachKategorie.get(kategorie);
                        float durchschnitt = summe/anzahl;
                        kategorieText.append(kategorie + ": " +durchschnitt + " von 10 bei " + anzahl +" Bewertung/en\n");
                    }
                    tvDurchschnittKategorie.setText(kategorieText.toString().trim());



                    bewerteteEntscheidungen.sort(
                            (entscheidung1, entscheidung2) ->
                                    Double.compare(
                                            entscheidung2.getBewertung(),
                                            entscheidung1.getBewertung()
                                    )
                    );
                    StringBuilder besteText = new StringBuilder();
                    int anzahlAnzeigen = Math.min(3, anzahlBewertet);
                    for(int i = 0; i < anzahlAnzeigen; i++){
                        Decision entscheidung = bewerteteEntscheidungen.get(i);
                        besteText.append(i+1 + ". "+ entscheidung.getTitel()+ " - " + entscheidung.getBewertung() + " von 10\n");
                    }
                    tvBesteEntscheidungen.setText(besteText);

                    StringBuilder schlechtesteText = new StringBuilder();
                    for(int i = 0; i < anzahlAnzeigen; i++){
                        int index = bewerteteEntscheidungen.size() - 1 - i;
                        Decision entscheidung = bewerteteEntscheidungen.get(index);
                        schlechtesteText.append(i+1 + ". "+ entscheidung.getTitel()+ " - " + entscheidung.getBewertung() + " von 10\n");
                    }
                    tvSchlechtesteEntscheidungen.setText(schlechtesteText);


                } else {
                    tvDurchschnittGesamt.setText("Du hast noch keine bewerteten Entscheidungen!");
                    tvDurchschnittStimmung.setText("Du hast noch keine bewerteten Entscheidungen!");
                    tvBesteEntscheidungen.setText("Du hast noch keine bewerteten Entscheidungen!");
                    tvSchlechtesteEntscheidungen.setText("Du hast noch keine bewerteten Entscheidungen!");
                    tvDurchschnittKategorie.setText("Du hast noch keine bewerteten Entscheidungen!");

                }







        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });





        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_statistics);

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