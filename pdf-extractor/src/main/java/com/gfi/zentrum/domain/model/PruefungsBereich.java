package com.gfi.zentrum.domain.model;

import java.util.List;

public record PruefungsBereich(
        String name,
        List<Aufgabe> aufgaben
) {
}
