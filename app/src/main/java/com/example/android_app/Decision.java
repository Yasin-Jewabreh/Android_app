package com.example.android_app;

import java.util.List;
import java.util.Map;

public class Decision {
    private String decisionID;
    private String userID;
    private String titel;
    private String kategorie;
    private String beschreibung;
    private String stimmung;
    private long erstellAm;
    private long erinnerungAm;
    private long bewertetAm;
    private float bewertung;
    private boolean istBewertet;
    private String entscheidung;
    private List<String> optionen;

    private List<String> kriterien;
    private Map<String, Integer> kriterienGewichtung;
    private Map<String, Map<String, Integer>> scoreMatrix;

    public Decision(String decisionID, String userID, String titel, String kategorie, String beschreibung, String stimmung, long erstellAm, long erinnerungAm, long bewertetAm, float bewertung, boolean istBewertet, String entscheidung, List<String> optionen, List<String> kriterien, Map<String, Integer> kriterienGewichtung, Map<String, Map<String, Integer>> scoreMatrix) {
        this.decisionID = decisionID;
        this.userID = userID;
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
        this.kriterien = kriterien;
        this.kriterienGewichtung = kriterienGewichtung;
        this.scoreMatrix = scoreMatrix;
    }

    public Decision() {}

    public String berechneGewinner(){
        String besteOption ="";
        int maxScore = -1;

        for (String option : scoreMatrix.keySet()) {
            int currentTotal = 0;

            Map<String, Integer> scoresForOption = scoreMatrix.get(option);

            for (String kriterium : kriterienGewichtung.keySet()) {
                int gewicht = kriterienGewichtung.get(kriterium);
                int score = scoresForOption.getOrDefault(kriterium, 0);
                currentTotal += (score*gewicht);
            }

            if (currentTotal > maxScore) {
                maxScore = currentTotal;
                besteOption = option;
            }
        }
        return besteOption;
    }

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

    public List<String> getKriterien() {
        return kriterien;
    }

    public void setKriterien(List<String> kriterien) {
        this.kriterien = kriterien;
    }

    public Map<String, Integer> getKriterienGewichtung() {
        return kriterienGewichtung;
    }

    public void setKriterienGewichtung(Map<String, Integer> kriterienGewichtung) {
        this.kriterienGewichtung = kriterienGewichtung;
    }

    public Map<String, Map<String, Integer>> getScoreMatrix() {
        return scoreMatrix;
    }

    public void setScoreMatrix(Map<String, Map<String, Integer>> scoreMatrix) {
        this.scoreMatrix = scoreMatrix;
    }
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
