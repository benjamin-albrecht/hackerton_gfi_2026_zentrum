package com.gfi.zentrum.model;

public class Aufgabe {
    private String name;
    private String struktur;
    private Termin termin;
    private String hilfmittel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStruktur() {
        return struktur;
    }

    public void setStruktur(String struktur) {
        this.struktur = struktur;
    }

    public Termin getTermin() {
        return termin;
    }

    public void setTermin(Termin termin) {
        this.termin = termin;
    }

    public String getHilfmittel() {
        return hilfmittel;
    }

    public void setHilfmittel(String hilfmittel) {
        this.hilfmittel = hilfmittel;
    }
}
