package com.gfi.zentrum.domain.model;

import java.util.List;

public record Beruf(
        String beschreibung,
        List<Integer> berufNr,
        List<PruefungsBereich> pruefungsBereich
) {
}
