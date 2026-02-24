package com.gfi.zentrum.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Beruf {
    private String beschreibung;
    private List<Integer> berufNr;

    @JsonProperty("prüfungsBereich")
    private List<PruefungsBereich> pruefungsBereich;

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public List<Integer> getBerufNr() {
        return berufNr;
    }

    public void setBerufNr(List<Integer> berufNr) {
        this.berufNr = berufNr;
    }

    public List<PruefungsBereich> getPruefungsBereich() {
        return pruefungsBereich;
    }

    public void setPruefungsBereich(List<PruefungsBereich> pruefungsBereich) {
        this.pruefungsBereich = pruefungsBereich;
    }
}
