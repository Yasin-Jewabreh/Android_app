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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RateDecisionActivity extends AppCompatActivity {

    // Aktueller Wert der Bewertung
    private float bewertung;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rate_decision);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views initialisieren
        TextView tvTitel = findViewById(R.id.tvCardTitel);
        TextView tvKategorie = findViewById(R.id.tvCardKategorie);
        TextView tvStimmung = findViewById(R.id.tvCardStimmung);
        TextView tvNotizen = findViewById(R.id.tvCardNotizen);
        TextView tvEntscheidung = findViewById(R.id.tvCardEntscheidung);
        TextView tvBewertungsWert = findViewById(R.id.tvBewertungsWert);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        Slider slider = findViewById(R.id.sliderBewertung);
        Button btnSpeichern = findViewById(R.id.btnBewertungSpeichern);

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
            Intent intent = new Intent(RateDecisionActivity.this, LoginActivity.class);
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
            Toast.makeText(RateDecisionActivity.this, "Entscheidung konnte nicht gefunden werden!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Aktuellen Slider-Wert als Bewertung übernehmen
        bewertung = slider.getValue();
        tvBewertungsWert.setText(bewertung + " von 10");

        // Bewertungswert beim Bewegen des Sliders aktualisieren
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float v, boolean b) {
                bewertung = v;
                tvBewertungsWert.setText(bewertung + " von 10");
            }
        });

        // Entscheidung einmalig aus Firebase laden
        ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Prüfen, ob die Entscheidung existiert
                if (!snapshot.exists()) {
                    Toast.makeText(RateDecisionActivity.this, "Entscheidung konnte nicht gefunden werden!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Firebase-Daten in ein Decision-Objekt laden
                Decision decision = snapshot.getValue(Decision.class);

                if (decision == null) {
                    Toast.makeText(RateDecisionActivity.this, "Entscheidung konnte nicht geladen werden!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Daten der Entscheidung anzeigen
                tvTitel.setText(decision.getTitel());
                tvKategorie.setText("Kategorie: " + decision.getKategorie());
                tvStimmung.setText("Deine Stimmung: " + decision.getStimmung());
                tvEntscheidung.setText("Entscheidung: " + decision.getEntscheidung());

                // Optionale Notizen anzeigen
                if (decision.getBeschreibung() != null && !decision.getBeschreibung().isEmpty()) {
                    tvNotizen.setText("Notizen:\n" + decision.getBeschreibung());
                } else {
                    tvNotizen.setText("Notizen: /");
                }

                // Bereits vorhandene Bewertung in den Slider laden
                if (decision.isIstBewertet()) {
                    bewertung = decision.getBewertung();
                    slider.setValue(bewertung);
                    tvBewertungsWert.setText(bewertung + " von 10");
                }

                // Vorzeitige Bewertung bestätigen lassen
                if (!decision.isIstBewertet() && System.currentTimeMillis() < decision.getErinnerungAm()) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                    String formatiertesDatum = sdf.format(new java.util.Date(decision.getErinnerungAm()));

                    new MaterialAlertDialogBuilder(RateDecisionActivity.this).setTitle("Heute wolltest du die Entscheidung noch nicht bewerten").setMessage("Als Datum für die Bewertung ist der " + formatiertesDatum + ". Möchtest du dennoch fortfahren?").setNegativeButton("Dennoch bewerten", null).setPositiveButton("Zurück", (dialog, which) -> {
                        finish();
                    }).setCancelable(false).show();
                }
            }

            // Fehler beim Laden der Entscheidung
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateDecisionActivity.this, "Entscheidung konnte nicht geladen werden: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Bewertung in Firebase speichern
        btnSpeichern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("bewertung", bewertung);
                map.put("istBewertet", true);
                map.put("bewertetAm", System.currentTimeMillis());

                ref.child(key).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(v.getContext(), "Bewertung erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(v.getContext(), "Deine Bewertung konnte leider nicht gespeichert werden: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}