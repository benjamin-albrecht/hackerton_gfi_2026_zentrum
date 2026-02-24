package com.gfi.zentrum.adapter.out.persistence.entity;

import java.util.List;

public record ExtractionEntity(
        String id,
        String sourceFileName,
        String extractedAt,
        List<BerufEntity> berufe,
        VerificationEntity verification
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

    public record VerificationEntity(
            boolean valid,
            List<VerificationIssueEntity> issues,
            String verifiedAt
    ) {
    }

    public record VerificationIssueEntity(
            String severity,
            String field,
            String message
    ) {
    }
}
