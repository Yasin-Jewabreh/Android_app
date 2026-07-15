package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AllDecisionsActivity extends AppCompatActivity {

    private FirebaseRecyclerAdapter<Decision, MyViewHolder> adapterOffen;
    private FirebaseRecyclerAdapter<Decision, MyViewHolder> adapterBewertet;

    private RecyclerView rvOffen, rvBewertet;

    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_decisions);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            currentUserId = mAuth.getCurrentUser().getUid();
            ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference()
                    .child("User")
                    .child(currentUserId)
                    .child("Decisions");
        } else {
            Toast.makeText(this, "Fehler: User nicht eingeloggt!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvOffen = findViewById(R.id.rvOffeneDecisions);
        rvOffen.setLayoutManager(new LinearLayoutManager(this));

        rvBewertet = findViewById(R.id.rvBewerteteDecisions);
        rvBewertet.setLayoutManager(new LinearLayoutManager(this));

        Query queryOffen = ref.orderByChild("istBewertet").equalTo(false);
        FirebaseRecyclerOptions<Decision> optionsOffen = new FirebaseRecyclerOptions.Builder<Decision>()
                .setQuery(queryOffen, Decision.class)
                .build();

        adapterOffen = new FirebaseRecyclerAdapter<Decision, MyViewHolder>(optionsOffen) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Decision model) {
                String key = getRef(position).getKey();
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), DetailsDecisionActivity.class);
                        intent.putExtra("key", key);
                        startActivity(intent);
                    }
                });

                holder.titel.setText(model.getTitel());
                holder.kategorie.setText("Kategorie: " + model.getKategorie());
                holder.entscheidung.setText("Entscheidung: " + model.getEntscheidung());
                long erinnerungZeit = model.getErinnerungAm();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                String formatiertesDatum = sdf.format(new java.util.Date(erinnerungZeit));
                holder.erinnerungAm.setText("Am " + formatiertesDatum + " bewerten");
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_decision_list_layout, parent, false);
                return new MyViewHolder(v);
            }
        };

        Query queryBewertet = ref.orderByChild("istBewertet").equalTo(true);
        FirebaseRecyclerOptions<Decision> optionsBewertet = new FirebaseRecyclerOptions.Builder<Decision>()
                .setQuery(queryBewertet, Decision.class)
                .build();

        adapterBewertet = new FirebaseRecyclerAdapter<Decision, MyViewHolder>(optionsBewertet) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Decision model) {
                holder.titel.setText(model.getTitel());
                holder.kategorie.setText("Kategorie: " + model.getKategorie());
                holder.entscheidung.setText("Entscheidung: " + model.getEntscheidung());
                long bewertetZeit = model.getBewertetAm();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                String formatiertesDatum = sdf.format(new java.util.Date(bewertetZeit));
                holder.erinnerungAm.setText("Am " + formatiertesDatum + " bewertet");




            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_decision_list_layout, parent, false);
                return new MyViewHolder(v);
            }
        };

        adapterOffen.startListening();
        rvOffen.setAdapter(adapterOffen);

        adapterBewertet.startListening();
        rvBewertet.setAdapter(adapterBewertet);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final FloatingActionButton addDecisionButton = findViewById(R.id.addDecisionButton);

        addDecisionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllDecisionsActivity.this, AddDecisionActivity.class);
                startActivity(intent);
            }
        });

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_list);

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterOffen != null) adapterOffen.stopListening();
        if (adapterBewertet != null) adapterBewertet.stopListening();
    }
}