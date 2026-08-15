package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AllDecisionsActivity extends AppCompatActivity {

    // FirebaseRecyclerAdapter für offene und bewertete Entscheidungen
    private FirebaseRecyclerAdapter<Decision, MyViewHolder> adapterOffen;
    private FirebaseRecyclerAdapter<Decision, MyViewHolder> adapterBewertet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_decisions);

        // Views initialisieren
        RecyclerView rvOffen = findViewById(R.id.rvOffeneDecisions);
        RecyclerView rvBewertet = findViewById(R.id.rvBewerteteDecisions);
        FloatingActionButton addDecisionButton = findViewById(R.id.addDecisionButton);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Firebase Authentication initialisieren und aktuellen Benutzer prüfen
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(AllDecisionsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Datenbankreferenz für die Entscheidungen des aktuellen Benutzers erstellen
        String currentUserId = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User").child(currentUserId).child("Decisions");

        // RecyclerViews konfigurieren
        rvOffen.setLayoutManager(new LinearLayoutManager(this));
        rvBewertet.setLayoutManager(new LinearLayoutManager(this));

        rvOffen.setItemAnimator(null);
        rvBewertet.setItemAnimator(null);

        // Firebase-Abfrage für offene Entscheidungen erstellen
        Query queryOffen = ref.orderByChild("istBewertet").equalTo(false);
        FirebaseRecyclerOptions<Decision> optionsOffen = new FirebaseRecyclerOptions.Builder<Decision>().setQuery(queryOffen, Decision.class).build();

        // Adapter für offene Entscheidungen erstellen
        adapterOffen = new FirebaseRecyclerAdapter<Decision, MyViewHolder>(optionsOffen) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Decision model) {
                String key = getRef(position).getKey();

                // Beim Anklicken die Detailansicht der Entscheidung öffnen
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AllDecisionsActivity.this, DetailsDecisionActivity.class);
                        intent.putExtra("key", key);
                        startActivity(intent);
                    }
                });

                // Entscheidungsdaten in der Liste anzeigen
                holder.titel.setText(model.getTitel());
                holder.kategorie.setText("Kategorie: " + model.getKategorie());
                holder.entscheidung.setText("Entscheidung: " + model.getEntscheidung());

                // Erinnerungsdatum formatieren und anzeigen
                long erinnerungZeit = model.getErinnerungAm();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                String formatiertesDatum = sdf.format(new java.util.Date(erinnerungZeit));
                holder.erinnerungAm.setText("Am " + formatiertesDatum + " bewerten");
            }

            // Layout für einen Eintrag der Liste erstellen
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_decision_list_layout, parent, false);
                return new MyViewHolder(v);
            }
        };

        // Firebase-Abfrage für bewertete Entscheidungen erstellen
        Query queryBewertet = ref.orderByChild("istBewertet").equalTo(true);
        FirebaseRecyclerOptions<Decision> optionsBewertet = new FirebaseRecyclerOptions.Builder<Decision>().setQuery(queryBewertet, Decision.class).build();

        // Adapter für bewertete Entscheidungen erstellen
        adapterBewertet = new FirebaseRecyclerAdapter<Decision, MyViewHolder>(optionsBewertet) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Decision model) {
                String key = getRef(position).getKey();
                boolean bewertet = true;

                // Beim Anklicken die Detailansicht der bewerteten Entscheidung öffnen
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AllDecisionsActivity.this, DetailsDecisionActivity.class);
                        intent.putExtra("key", key);
                        intent.putExtra("bewertet", bewertet);
                        startActivity(intent);
                    }
                });

                // Entscheidungsdaten in der Liste anzeigen
                holder.titel.setText(model.getTitel());
                holder.kategorie.setText("Kategorie: " + model.getKategorie());
                holder.entscheidung.setText("Entscheidung: " + model.getEntscheidung());

                // Bewertungsdatum formatieren und anzeigen
                long bewertetZeit = model.getBewertetAm();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                String formatiertesDatum = sdf.format(new java.util.Date(bewertetZeit));
                holder.erinnerungAm.setText("Am " + formatiertesDatum + " bewertet");
            }

            // Layout für einen Eintrag der Liste erstellen
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_decision_list_layout, parent, false);
                return new MyViewHolder(v);
            }
        };

        // Adapter mit den RecyclerViews verbinden
        rvOffen.setAdapter(adapterOffen);
        rvBewertet.setAdapter(adapterBewertet);

        // Neue Entscheidung erstellen
        addDecisionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllDecisionsActivity.this, AddDecisionActivity.class);
                startActivity(intent);
            }
        });

        // Liste als aktuellen Punkt der Bottom Navigation markieren
        bottomNav.setSelectedItemId(R.id.nav_list);

        // Navigation zwischen den Hauptbereichen der App
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(AllDecisionsActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_list) {
                return true;
            } else if (id == R.id.nav_statistics) {
                startActivity(new Intent(AllDecisionsActivity.this, StatisticsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(AllDecisionsActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Firebase-Adapter starten, sobald die Activity sichtbar wird
    @Override
    protected void onStart() {
        super.onStart();
        if (adapterOffen != null) adapterOffen.startListening();
        if (adapterBewertet != null) adapterBewertet.startListening();
    }

    // Firebase-Adapter stoppen, sobald die Activity nicht mehr sichtbar ist
    @Override
    protected void onStop() {
        super.onStop();
        if (adapterOffen != null) adapterOffen.stopListening();
        if (adapterBewertet != null) adapterBewertet.stopListening();
    }
}