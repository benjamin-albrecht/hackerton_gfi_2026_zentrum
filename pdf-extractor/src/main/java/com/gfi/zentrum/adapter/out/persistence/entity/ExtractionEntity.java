package com.gfi.zentrum.adapter.out.persistence.entity;

import java.util.List;

public record ExtractionEntity(
        String id,
        String sourceFileName,
        String extractedAt,
        List<BerufEntity> berufe
) {

    public record BerufEntity(
            String beschreibung,
            List<Integer> berufNr,
            List<PruefungsBereichEntity> pruefungsBereich
    ) {
    }

    public record PruefungsBereichEntity(
            String name,
            List<AufgabeEntity> aufgaben
    ) {
    }

    public record AufgabeEntity(
            String name,
            String struktur,
            TerminEntity termin,
            String hilfmittel
    ) {
    }

    public record TerminEntity(
            String datum,
            String uhrzeitVon,
            String uhrzeitBis,
            int dauer
    ) {
    }
}
