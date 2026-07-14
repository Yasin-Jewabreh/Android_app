package com.example.android_app;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView titel, kategorie, entscheidung;
    Button bewerten, bearbeiten;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        titel = itemView.findViewById(R.id.tvCardTitel);
        kategorie = itemView.findViewById(R.id.tvCardKategorie);
        entscheidung = itemView.findViewById(R.id.tvCardEntscheidung);
        bewerten = itemView.findViewById(R.id.btnBewerten);
        bearbeiten = itemView.findViewById(R.id.btnBearbeiten);


    }
}
