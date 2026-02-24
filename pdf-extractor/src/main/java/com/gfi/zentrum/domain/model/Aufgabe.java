package com.gfi.zentrum.domain.model;

public record Aufgabe(
        String name,
        String struktur,
        Termin termin,
        String hilfmittel
) {
}
