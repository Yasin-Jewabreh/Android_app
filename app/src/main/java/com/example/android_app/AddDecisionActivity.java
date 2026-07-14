package com.example.android_app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddDecisionActivity extends AppCompatActivity {
    private FirebaseRecyclerOptions<Decision> options;
    private FirebaseRecyclerAdapter<Decision,MyViewHolder> adapter;
    private RecyclerView recyclerView;
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private List<String> meineOptionen = new ArrayList<>();
    private ArrayAdapter<String> optionenAdapter;

    private String titel, kategorie, entscheidung, beschreibung, stimmung ;
    private long erinnerungsTimestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_decision);

        //Datenbanken initialisieren
        mAuth = FirebaseAuth.getInstance();

        //aktuellen User speichern
        if (mAuth.getCurrentUser() != null){
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User").child(currentUserId).child("Decisions");

        //Kategorien anzeigen
        AutoCompleteTextView categoryDropdown = findViewById(R.id.categoryDropdown);
        ArrayList<String> kategorieListe = new ArrayList<>(Arrays.asList("Arbeit", "Privat", "Finanzen"));
        ArrayAdapter<String> adapterKategorie = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, kategorieListe);
        categoryDropdown.setAdapter(adapterKategorie);

        //Moods anzeigen
        AutoCompleteTextView moodDropdown = findViewById(R.id.moodDropdown);
        ArrayList<String> moodListe = new ArrayList<>(Arrays.asList("Motiviert", "Gelassen", "Neutral", "Fokussiert", "Gestresst", "Überfordert", "Müde"));
        ArrayAdapter<String> adapterMood = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, moodListe);
        moodDropdown.setAdapter(adapterMood);

        //Button, Text und Listview für die Optionen Initialsieren
        final EditText titelText = findViewById(R.id.decisionTitel);
        final EditText beschreibungText = findViewById(R.id.decisionBeschreibung);
        final Button addOption = findViewById(R.id.addOptionButton);
        final EditText optionName = findViewById(R.id.inputOptionName);
        final ListView optionenListview = findViewById(R.id.listView);
        final TextView entscheidungText = findViewById(R.id.entscheidungText);
        final Button speichern = findViewById(R.id.btnSpeichern);
        final Button abbrechen = findViewById(R.id.btnAbbrechen);
        final EditText erinnerungAm = findViewById(R.id.decisionDatum);

        optionenAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, meineOptionen);
        optionenListview.setAdapter(optionenAdapter);

        //On Click Listener für den Hinzufügen Button von Optionen
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String option = optionName.getText().toString().trim();
                if (!option.isEmpty()) {
                    meineOptionen.add(option);
                    optionenAdapter.notifyDataSetChanged();
                    optionName.setText("");
                }
            }
        });

        optionenListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(AddDecisionActivity.this)
                        .setTitle("Was möchtest du tun?")
                        .setMessage("Möchtest du dich für " + meineOptionen.get(position) + " entscheiden oder diese Option entfernen?")
                        .setNegativeButton("Entscheiden", (dialog, which) -> {
                            entscheidung = meineOptionen.get(position);
                            final TextView entscheidungText = findViewById(R.id.entscheidungText);
                            entscheidungText.setText("Entscheidung: "+ entscheidung);
                        })
                        .setPositiveButton("Löschen", (dialog, which) -> {
                            meineOptionen.remove(position);
                            entscheidungText.setText("");
                            optionenAdapter.notifyDataSetChanged();
                        }).show();

            }
        });

        erinnerungsTimestamp = 0;
        erinnerungAm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();

                new android.app.DatePickerDialog(AddDecisionActivity.this, (view, year, month, dayOfMonth) -> {
                    new android.app.TimePickerDialog(AddDecisionActivity.this, (timeView, hourOfDay, minute) -> {
                        c.set(year, month, dayOfMonth, hourOfDay, minute);
                        erinnerungsTimestamp = c.getTimeInMillis();

                        erinnerungAm.setText(dayOfMonth +"." + (month+1) + "." + year + " " + hourOfDay + ":" + minute);
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        speichern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titel = titelText.getText().toString().trim();
                String beschreibung = beschreibungText.getText().toString().trim();
                String kategorie = categoryDropdown.getText().toString().trim();
                String stimmung = moodDropdown.getText().toString().trim();
                long erstelltAm = System.currentTimeMillis();
                long erinnerungAm = erinnerungsTimestamp;
                String decisionID = ref.push().getKey();
                String entscheidung = entscheidungText.getText().toString().replace("Entscheidung: ", "").trim();

                if (titel.isEmpty() || kategorie.isEmpty() || stimmung.isEmpty() || erinnerungAm == 0 || entscheidung.isEmpty()) {
                    Toast.makeText(v.getContext(), "Bitte alle nötigen Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Decision neueEntscheidung = new Decision();

                neueEntscheidung.setTitel(titel);
                neueEntscheidung.setBeschreibung(beschreibung);
                neueEntscheidung.setKategorie(kategorie);
                neueEntscheidung.setStimmung(stimmung);
                neueEntscheidung.setErinnerungAm(erinnerungAm);
                neueEntscheidung.setErstellAm(erstelltAm);
                neueEntscheidung.setUserID(currentUserId);
                neueEntscheidung.setDecisionID(decisionID);
                neueEntscheidung.setEntscheidung(entscheidung);
                neueEntscheidung.setIstBewertet(false);

                ref.child(decisionID).setValue(neueEntscheidung).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(v.getContext(), "Entscheidung erfolgreich eingefügt!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(v.getContext(), "Speichern der Entscheidung fehlgeschlagen!", Toast.LENGTH_SHORT).show();

                    }
                });


            }
        });
        abbrechen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}