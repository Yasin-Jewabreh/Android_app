package com.example.android_app;

import static java.lang.Long.parseLong;

import android.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RateDecisionActivity extends AppCompatActivity {
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    String currentUserId;
    TextView tvTitel, tvKategorie, tvStimmung, tvNotizen, tvEntscheidung;
    String titel, kategorie, stimmung, notizen, entscheidung, formatiertesDatum;
    MaterialToolbar topAppBar;
    Slider slider;
    long erinnerungAm = 0;
    TextView tvBewertungsWert;
    Button btnSpeichern;
    float bewertung;

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
        tvTitel = findViewById(R.id.tvCardTitel);
        tvKategorie= findViewById(R.id.tvCardKategorie);
        tvStimmung= findViewById(R.id.tvCardStimmung);
        tvNotizen = findViewById(R.id.tvCardNotizen);
        tvEntscheidung = findViewById(R.id.tvCardEntscheidung);

        topAppBar= findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference()
                .child("User")
                .child(currentUserId)
                .child("Decisions");

        tvBewertungsWert = findViewById(R.id.tvBewertungsWert);

        slider = findViewById(R.id.sliderBewertung);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float v, boolean b) {
                bewertung = v;
                String bewertungText = new Float(v).toString();
                tvBewertungsWert.setText(bewertung + " von 10");
            }
        });
        String key = getIntent().getStringExtra("key");
        ref.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                titel = snapshot.child("titel").getValue().toString();
                kategorie = snapshot.child("kategorie").getValue().toString();
                stimmung = snapshot.child("stimmung").getValue().toString();
                notizen = snapshot.child("beschreibung").getValue().toString();
                entscheidung = snapshot.child("entscheidung").getValue().toString();
                erinnerungAm = parseLong(snapshot.child("erinnerungAm").getValue().toString());

                tvTitel.setText(titel);
                tvKategorie.setText("Kategorie: "+kategorie);
                tvStimmung.setText("Deine Stimmung: "+stimmung);
                tvEntscheidung.setText("Entscheidung: "+entscheidung);
                if (!notizen.isEmpty()) { tvNotizen.setText("Notizen:\n" +notizen);} else {tvNotizen.setText("Notizen: /");}

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                formatiertesDatum = sdf.format(new java.util.Date(erinnerungAm));

                if (System.currentTimeMillis() < erinnerungAm) {
                    new MaterialAlertDialogBuilder(RateDecisionActivity.this)
                            .setTitle("Heute wolltest du die Entscheidung noch nicht bewerten")
                            .setMessage("Als Datum für die Bewertung ist der "+ formatiertesDatum+". Möchtest du dennoch fortfahren?")
                            .setNegativeButton("Dennoch bewerten", null)
                            .setPositiveButton("Zurück", (dialog, which) -> {finish();})
                            .setCancelable(false)
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        btnSpeichern = findViewById(R.id.btnBewertungSpeichern);

        btnSpeichern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("bewertung", bewertung);
                map.put("istBewertet", true);
                map.put("bewertetAm", System.currentTimeMillis());
                ref.child(key).updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(v.getContext(), "Bewertung erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RateDecisionActivity.this, AllDecisionsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(v.getContext(), "Deine Bewertung konnte leider nicht gespeichert werden", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

    }


}