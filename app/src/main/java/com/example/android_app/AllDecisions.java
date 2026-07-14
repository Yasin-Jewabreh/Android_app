package com.example.android_app;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AllDecisions extends AppCompatActivity {

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
        rvOffen.setHasFixedSize(true);
        rvOffen.setLayoutManager(new LinearLayoutManager(this));

        rvBewertet = findViewById(R.id.rvBewerteteDecisions);
        rvBewertet.setHasFixedSize(true);
        rvBewertet.setLayoutManager(new LinearLayoutManager(this));

        Query queryOffen = ref.orderByChild("bewertet").equalTo(false);
        FirebaseRecyclerOptions<Decision> optionsOffen = new FirebaseRecyclerOptions.Builder<Decision>()
                .setQuery(queryOffen, Decision.class)
                .build();

        adapterOffen = new FirebaseRecyclerAdapter<Decision, MyViewHolder>(optionsOffen) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Decision model) {
                holder.titel.setText(model.getTitel());
                holder.kategorie.setText("Kategorie: " + model.getKategorie());

                if (model.getEntscheidung() != null && !model.getEntscheidung().isEmpty()) {
                    holder.entscheidung.setText("Entscheidung: " + model.getEntscheidung());
                } else {
                    holder.entscheidung.setText("Entscheidung: Noch offen");
                }

                holder.bewerten.setEnabled(true);
                holder.bewerten.setText("Bewerten");
                holder.bewerten.setOnClickListener(v -> Toast.makeText(AllDecisions.this, "Bewerten für: " + model.getTitel(), Toast.LENGTH_SHORT).show());
                holder.bearbeiten.setOnClickListener(v -> Toast.makeText(AllDecisions.this, "Bearbeiten für: " + model.getTitel(), Toast.LENGTH_SHORT).show());
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_layout, parent, false);
                return new MyViewHolder(v);
            }
        };

        Query queryBewertet = ref.orderByChild("bewertet").equalTo(true);
        FirebaseRecyclerOptions<Decision> optionsBewertet = new FirebaseRecyclerOptions.Builder<Decision>()
                .setQuery(queryBewertet, Decision.class)
                .build();

        adapterBewertet = new FirebaseRecyclerAdapter<Decision, MyViewHolder>(optionsBewertet) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Decision model) {
                holder.titel.setText(model.getTitel());
                holder.kategorie.setText("Kategorie: " + model.getKategorie());
                holder.entscheidung.setText("Entscheidung: " + model.getEntscheidung());

                holder.bewerten.setText("Bewertet ✓");
                holder.bewerten.setEnabled(false);

                holder.bearbeiten.setOnClickListener(v -> Toast.makeText(AllDecisions.this, "Bearbeiten für: " + model.getTitel(), Toast.LENGTH_SHORT).show());
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_layout, parent, false);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterOffen != null) adapterOffen.stopListening();
        if (adapterBewertet != null) adapterBewertet.stopListening();
    }
}