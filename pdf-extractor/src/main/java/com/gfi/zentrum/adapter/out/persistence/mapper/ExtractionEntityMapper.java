package com.gfi.zentrum.adapter.out.persistence.mapper;

import com.gfi.zentrum.adapter.out.persistence.entity.ExtractionEntity;
import com.gfi.zentrum.adapter.out.persistence.entity.ExtractionEntity.*;
import com.gfi.zentrum.domain.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class ExtractionEntityMapper {

    public ExtractionEntity toEntity(ExtractionResult result) {
        List<BerufEntity> berufe = result.berufe().stream()
                .map(this::toBerufEntity)
                .toList();
        return new ExtractionEntity(
                result.id().toString(),
                result.sourceFileName(),
                result.extractedAt().toString(),
                berufe
        );
    }

    public ExtractionResult toDomain(ExtractionEntity entity) {
        List<Beruf> berufe = entity.berufe().stream()
                .map(this::toBerufDomain)
                .toList();
        return new ExtractionResult(
                ExtractionId.of(entity.id()),
                entity.sourceFileName(),
                Instant.parse(entity.extractedAt()),
                berufe
        );
    }

    private BerufEntity toBerufEntity(Beruf beruf) {
        List<PruefungsBereichEntity> bereiche = beruf.pruefungsBereich().stream()
                .map(this::toBereichEntity)
                .toList();
        return new BerufEntity(beruf.beschreibung(), beruf.berufNr(), bereiche);
    }

    private PruefungsBereichEntity toBereichEntity(PruefungsBereich bereich) {
        List<AufgabeEntity> aufgaben = bereich.aufgaben().stream()
                .map(this::toAufgabeEntity)
                .toList();
        return new PruefungsBereichEntity(bereich.name(), aufgaben);
    }

    private AufgabeEntity toAufgabeEntity(Aufgabe aufgabe) {
        Termin t = aufgabe.termin();
        TerminEntity termin = new TerminEntity(
                t.datum().toString(),
                t.uhrzeitVon().toString(),
                t.uhrzeitBis().toString(),
                t.dauer()
        );
        return new AufgabeEntity(aufgabe.name(), aufgabe.struktur(), termin, aufgabe.hilfmittel());
    }

    private Beruf toBerufDomain(BerufEntity entity) {
        List<PruefungsBereich> bereiche = entity.pruefungsBereich().stream()
                .map(this::toBereichDomain)
                .toList();
        return new Beruf(entity.beschreibung(), entity.berufNr(), bereiche);
    }

    private PruefungsBereich toBereichDomain(PruefungsBereichEntity entity) {
        List<Aufgabe> aufgaben = entity.aufgaben().stream()
                .map(this::toAufgabeDomain)
                .toList();
        return new PruefungsBereich(entity.name(), aufgaben);
    }

    private Aufgabe toAufgabeDomain(AufgabeEntity entity) {
        TerminEntity te = entity.termin();
        Termin termin = new Termin(
                parseDate(te.datum()),
                parseTime(te.uhrzeitVon()),
                parseTime(te.uhrzeitBis()),
                te.dauer()
        );
        return new Aufgabe(entity.name(), entity.struktur(), termin, entity.hilfmittel());
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return LocalDate.of(2026, 1, 1);
        }
    }

    private LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(s);
        } catch (DateTimeParseException e) {
            return LocalTime.MIDNIGHT;
        }
    }
}
