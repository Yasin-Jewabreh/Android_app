package com.example.android_app;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    // Views eines einzelnen Entscheidungseintrags
    final TextView titel, kategorie, entscheidung, erinnerungAm;
    final View view;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        // Views aus dem Layout des Listeneintrags initialisieren
        titel = itemView.findViewById(R.id.tvCardTitel);
        kategorie = itemView.findViewById(R.id.tvCardKategorie);
        entscheidung = itemView.findViewById(R.id.tvCardEntscheidung);
        erinnerungAm = itemView.findViewById(R.id.tvCardErinnerungAm);
        view = itemView;
    }
}