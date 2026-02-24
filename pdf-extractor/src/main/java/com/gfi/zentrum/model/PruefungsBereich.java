package com.gfi.zentrum.model;

import java.util.List;

public class PruefungsBereich {
    private String name;
    private List<Aufgabe> aufgaben;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Aufgabe> getAufgaben() {
        return aufgaben;
    }

    public void setAufgaben(List<Aufgabe> aufgaben) {
        this.aufgaben = aufgaben;
    }
}
