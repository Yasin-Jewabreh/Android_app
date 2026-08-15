package com.example.android_app;

import java.util.List;
import java.util.Map;

public class Decision {

    // Grunddaten der Entscheidung
    private String decisionID;
    private String userID;
    private String titel;
    private String kategorie;
    private String beschreibung;
    private String stimmung;

    // Zeitangaben und Bewertung
    private long erstellAm;
    private long erinnerungAm;
    private long bewertetAm;
    private float bewertung;
    private boolean istBewertet;

    // Ergebnis und mögliche Optionen
    private String entscheidung;
    private List<String> optionen;

    public Decision(String userID, String decisionID, String titel, String kategorie, String beschreibung, String stimmung, long erstellAm, long erinnerungAm, long bewertetAm, float bewertung, boolean istBewertet, String entscheidung, List<String> optionen) {
        this.userID = userID;
        this.decisionID = decisionID;
        this.titel = titel;
        this.kategorie = kategorie;
        this.beschreibung = beschreibung;
        this.stimmung = stimmung;
        this.erstellAm = erstellAm;
        this.erinnerungAm = erinnerungAm;
        this.bewertetAm = bewertetAm;
        this.bewertung = bewertung;
        this.istBewertet = istBewertet;
        this.entscheidung = entscheidung;
        this.optionen = optionen;
    }

    // Leerer Konstruktor für Firebase
    public Decision() {}


    // Getter und Setter für die Entscheidungsdaten
    public String getDecisionID() {
        return decisionID;
    }

    public void setDecisionID(String decisionID) {
        this.decisionID = decisionID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getKategorie() {
        return kategorie;
    }

    public void setKategorie(String kategorie) {
        this.kategorie = kategorie;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getStimmung() {
        return stimmung;
    }

    public void setStimmung(String stimmung) {
        this.stimmung = stimmung;
    }

    public long getErstellAm() {
        return erstellAm;
    }

    public void setErstellAm(long erstellAm) {
        this.erstellAm = erstellAm;
    }

    public long getErinnerungAm() {
        return erinnerungAm;
    }

    public void setErinnerungAm(long erinnerungAm) {
        this.erinnerungAm = erinnerungAm;
    }

    public List<String> getOptionen() {
        return optionen;
    }

    public void setOptionen(List<String> optionen) {
        this.optionen = optionen;
    }

    // Getter und Setter für Entscheidung und Bewertung
    public String getEntscheidung() {
        return entscheidung;
    }

    public void setEntscheidung(String entscheidung) {
        this.entscheidung = entscheidung;
    }

    public boolean isIstBewertet() {
        return istBewertet;
    }

    public void setIstBewertet(boolean istBewertet) {
        this.istBewertet = istBewertet;
    }

    public long getBewertetAm() {
        return bewertetAm;
    }

    public void setBewertetAm(long bewertetAm) {
        this.bewertetAm = bewertetAm;
    }

    public float getBewertung() {
        return bewertung;
    }

    public void setBewertung(float bewertung) {
        this.bewertung = bewertung;
    }
}