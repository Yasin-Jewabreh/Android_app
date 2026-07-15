package com.example.android_app;

import static java.lang.Long.parseLong;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsDecisionActivity extends AppCompatActivity {

    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    String currentUserId;
    TextView tvTitel, tvKategorie, tvStimmung, tvNotizen, tvEntscheidung, tvErinnerungAm;
    String titel, kategorie, stimmung, notizen, entscheidung;
    long erinnerungAm;
    Button btnBewerten, btnBearbeiten;
    MaterialToolbar topAppBar;
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

        tvTitel = findViewById(R.id.tvCardTitel);
        tvKategorie= findViewById(R.id.tvCardKategorie);
        tvStimmung= findViewById(R.id.tvCardStimmung);
        tvNotizen = findViewById(R.id.tvCardNotizen);
        tvErinnerungAm = findViewById(R.id.tvCardErinnerungAm);
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
        btnBewerten = findViewById(R.id.btnBewerten);
        String key = getIntent().getStringExtra("key");

        btnBewerten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsDecisionActivity.this, RateDecisionActivity.class);
                intent.putExtra("key", key);
                startActivity(intent);
            }
        });
        btnBearbeiten = findViewById(R.id.btnBearbeiten);
        String request = "bearbeiten";
        btnBearbeiten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsDecisionActivity.this, AddDecisionActivity.class);
                intent.putExtra("key", key);
                intent.putExtra("request", request);
                startActivity(intent);
            }
        });

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
                if (!notizen.isEmpty()) { tvNotizen.setText("Notizen: " +notizen);} else {tvNotizen.setText("Notizen: /");}

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                String formatiertesDatum = sdf.format(new java.util.Date(erinnerungAm));
                tvErinnerungAm.setText("Am " + formatiertesDatum + " bewerten");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}