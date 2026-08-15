package com.example.android_app;

import android.app.AlertDialog;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AddDecisionActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // Optionen
    private List<String> meineOptionen = new ArrayList<>();
    private ArrayAdapter<String> optionenAdapter;

    // Daten der Entscheidung
    private String titel, kategorie, entscheidung, notizen, stimmung, decisionID, option, key;
    private long erinnerungsTimestamp, erstelltAm, erinnerungAm;
    private boolean bearbeiten = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_decision);

        // Intent-Daten auslesen und prüfen, ob eine Entscheidung bearbeitet wird
        key = getIntent().getStringExtra("key");
        bearbeiten = getIntent().getBooleanExtra("bearbeiten", false);

        // Views initialisieren
        final TextView addDecisionText = findViewById(R.id.addDecisionText);
        final EditText titelText = findViewById(R.id.decisionTitel);
        final EditText beschreibungText = findViewById(R.id.decisionBeschreibung);
        final AutoCompleteTextView categoryDropdown = findViewById(R.id.categoryDropdown);
        final AutoCompleteTextView moodDropdown = findViewById(R.id.moodDropdown);
        final Button addOption = findViewById(R.id.addOptionButton);
        final EditText optionName = findViewById(R.id.inputOptionName);
        final ListView optionenListview = findViewById(R.id.listView);
        final TextView entscheidungText = findViewById(R.id.entscheidungText);
        final Button speichern = findViewById(R.id.btnSpeichern);
        final Button abbrechen = findViewById(R.id.btnAbbrechen);
        final EditText etErinnerungAm = findViewById(R.id.decisionDatum);

        // Firebase initialisieren und aktuellen Benutzer prüfen
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(AddDecisionActivity.this, "Du bist nicht eingeloggt!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddDecisionActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance("https://android-app-d17b6-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("User").child(currentUserId).child("Decisions");

        // Prüfen, ob beim Bearbeiten eine Entscheidungs-ID vorhanden ist
        if (bearbeiten && key == null) {
            Toast.makeText(AddDecisionActivity.this, "Entscheidung konnte nicht gefunden werden!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kategorien für das Dropdown vorbereiten
        ArrayList<String> kategorieListe = new ArrayList<>(Arrays.asList("Arbeit & Karriere", "Finanzen", "Gesundheit", "Beziehung & Partnerschaft", "Familie", "Freunde & Soziales", "Wohnen", "Kaufentscheidungen", "Reisen & Freizeit", "Bildung & Lernen", "Persönliche Entwicklung", "Alltag & Organisation", "Sonstiges"));
        ArrayAdapter<String> adapterKategorie = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, kategorieListe);
        categoryDropdown.setAdapter(adapterKategorie);

        // Stimmungen für das Dropdown vorbereiten
        ArrayList<String> moodListe = new ArrayList<>(Arrays.asList("Motiviert", "Gelassen", "Neutral", "Fokussiert", "Unsicher", "Gestresst", "Frustriert", "Überfordert", "Müde"));
        ArrayAdapter<String> adapterMood = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, moodListe);
        moodDropdown.setAdapter(adapterMood);

        // Liste der Entscheidungsoptionen vorbereiten
        optionenAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, meineOptionen);
        optionenListview.setAdapter(optionenAdapter);

        // Überschrift im Bearbeitungsmodus anpassen
        if (bearbeiten) {
            addDecisionText.setText("Bearbeite deine Entscheidung");
        }

        // Neue Option zur Liste hinzufügen
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option = optionName.getText().toString().trim();

                if (!option.isEmpty()) {
                    meineOptionen.add(option);
                    optionenAdapter.notifyDataSetChanged();
                    optionName.setText("");
                }
            }
        });

        // Option auswählen oder löschen
        optionenListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(AddDecisionActivity.this).setTitle("Was möchtest du tun?").setMessage("Möchtest du dich für " + meineOptionen.get(position) + " entscheiden oder diese Option entfernen?").setNegativeButton("Entscheiden", (dialog, which) -> {
                    entscheidung = meineOptionen.get(position);
                    entscheidungText.setText("Entscheidung: " + entscheidung);
                }).setPositiveButton("Löschen", (dialog, which) -> {
                    String geloeschteOption = meineOptionen.get(position);

                    if (geloeschteOption.equals(entscheidung)) {
                        entscheidung = "";
                        entscheidungText.setText("");
                    }

                    meineOptionen.remove(position);
                    optionenAdapter.notifyDataSetChanged();
                }).show();
            }
        });

        // Erinnerungsdatum und Uhrzeit auswählen
        erinnerungsTimestamp = 0;

        etErinnerungAm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();

                if (erinnerungsTimestamp > System.currentTimeMillis()) {
                    c.setTimeInMillis(erinnerungsTimestamp);
                }

                android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(AddDecisionActivity.this, (view, year, month, dayOfMonth) -> {
                    new android.app.TimePickerDialog(AddDecisionActivity.this, (timeView, hourOfDay, minute) -> {
                        c.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                        c.set(Calendar.MILLISECOND, 0);

                        if (c.getTimeInMillis() < System.currentTimeMillis()) {
                            Toast.makeText(AddDecisionActivity.this, "Bitte wähle ein Datum und eine Uhrzeit in der Zukunft.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        erinnerungsTimestamp = c.getTimeInMillis();
                        etErinnerungAm.setText(dayOfMonth + "." + (month + 1) + "." + year + " " + hourOfDay + ":" + String.format("%02d", minute));
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                Calendar heute = Calendar.getInstance();
                heute.set(Calendar.HOUR_OF_DAY, 0);
                heute.set(Calendar.MINUTE, 0);
                heute.set(Calendar.SECOND, 0);
                heute.set(Calendar.MILLISECOND, 0);
                datePickerDialog.getDatePicker().setMinDate(heute.getTimeInMillis());

                datePickerDialog.show();
            }
        });

        // Entscheidung speichern oder vorhandene Entscheidung aktualisieren
        speichern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Eingaben aus den Feldern auslesen
                titel = titelText.getText().toString().trim();
                notizen = beschreibungText.getText().toString().trim();
                kategorie = categoryDropdown.getText().toString().trim();
                stimmung = moodDropdown.getText().toString().trim();
                erstelltAm = System.currentTimeMillis();
                erinnerungAm = erinnerungsTimestamp;
                entscheidung = entscheidungText.getText().toString().replace("Entscheidung: ", "").trim();

                // Pflichtfelder prüfen
                if (titel.isEmpty() || kategorie.isEmpty() || stimmung.isEmpty() || erinnerungAm == 0 || meineOptionen.isEmpty() || entscheidung.isEmpty()) {
                    Toast.makeText(v.getContext(), "Bitte alle nötigen Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Neue Entscheidung erstellen
                if (!bearbeiten) {
                    decisionID = ref.push().getKey();

                    if (decisionID == null) {
                        Toast.makeText(AddDecisionActivity.this, "Entscheidung konnte nicht erstellt werden!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Decision neueEntscheidung = new Decision();

                    neueEntscheidung.setTitel(titel);
                    neueEntscheidung.setBeschreibung(notizen);
                    neueEntscheidung.setKategorie(kategorie);
                    neueEntscheidung.setStimmung(stimmung);
                    neueEntscheidung.setErinnerungAm(erinnerungAm);
                    neueEntscheidung.setErstellAm(erstelltAm);
                    neueEntscheidung.setUserID(currentUserId);
                    neueEntscheidung.setDecisionID(decisionID);
                    neueEntscheidung.setEntscheidung(entscheidung);
                    neueEntscheidung.setIstBewertet(false);
                    neueEntscheidung.setBewertetAm(0);
                    neueEntscheidung.setBewertung(0);
                    neueEntscheidung.setOptionen(new ArrayList<>(meineOptionen));

                    ref.child(decisionID).setValue(neueEntscheidung).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(v.getContext(), "Entscheidung erfolgreich eingefügt!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(), "Speichern der Entscheidung fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    // Bestehende Entscheidung aktualisieren
                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();

                    hashMap.put("titel", titel);
                    hashMap.put("beschreibung", notizen);
                    hashMap.put("kategorie", kategorie);
                    hashMap.put("stimmung", stimmung);
                    hashMap.put("erinnerungAm", erinnerungAm);
                    hashMap.put("entscheidung", entscheidung);
                    hashMap.put("optionen", new ArrayList<>(meineOptionen));

                    ref.child(key).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(v.getContext(), "Änderungen erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(), "Änderungen konnten leider nicht gespeichert werden: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        // Vorgang abbrechen und Activity schließen
        abbrechen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Beim Bearbeiten vorhandene Daten einmalig aus Firebase laden
        if (bearbeiten) {
            ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    // Prüfen, ob die Entscheidung tatsächlich existiert
                    if (!snapshot.exists()) {
                        Toast.makeText(AddDecisionActivity.this, "Entscheidung konnte nicht gefunden werden!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Gespeicherte Grunddaten laden
                    titel = snapshot.child("titel").getValue().toString();
                    kategorie = snapshot.child("kategorie").getValue().toString();
                    stimmung = snapshot.child("stimmung").getValue().toString();

                    String gespeicherteBeschreibung = snapshot.child("beschreibung").getValue(String.class);
                    notizen = gespeicherteBeschreibung != null ? gespeicherteBeschreibung : "";

                    entscheidung = snapshot.child("entscheidung").getValue().toString();
                    erinnerungAm = Long.parseLong(snapshot.child("erinnerungAm").getValue().toString());
                    erinnerungsTimestamp = erinnerungAm;

                    // Gespeicherte Optionen laden
                    meineOptionen.clear();
                    DataSnapshot dataSnapshot = snapshot.child("optionen");

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        option = child.getValue().toString();
                        meineOptionen.add(option);
                    }

                    optionenAdapter.notifyDataSetChanged();

                    // Geladene Daten in den Eingabefeldern anzeigen
                    titelText.setText(titel);
                    categoryDropdown.setText(kategorie);
                    moodDropdown.setText(stimmung);
                    entscheidungText.setText("Entscheidung: " + entscheidung);
                    beschreibungText.setText(notizen);

                    // Erinnerungsdatum formatieren und anzeigen
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", java.util.Locale.getDefault());
                    String formatiertesDatum = sdf.format(new java.util.Date(erinnerungAm));
                    etErinnerungAm.setText("Am " + formatiertesDatum + " bewerten");
                }

                // Fehler beim Laden der bestehenden Entscheidung
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddDecisionActivity.this, "Entscheidung konnte nicht geladen werden: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}