package com.gfi.zentrum.adapter.in.rest.mapper;

import com.gfi.zentrum.adapter.in.rest.dto.*;
import com.gfi.zentrum.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExtractionDtoMapper {

    public ExtractionCreatedResponse toCreatedResponse(ExtractionResult result) {
        return new ExtractionCreatedResponse(
                result.id().toString(),
                result.sourceFileName(),
                result.extractedAt(),
                result.berufe().size()
        );
    }

    public ExtractionSummaryResponse toSummaryResponse(ExtractionResult result) {
        return new ExtractionSummaryResponse(
                result.id().toString(),
                result.sourceFileName(),
                result.extractedAt(),
                result.berufe().size()
        );
    }

    public ExtractionDetailResponse toDetailResponse(ExtractionResult result) {
        List<BerufResponse> berufe = result.berufe().stream()
                .map(this::toBerufResponse)
                .toList();
        return new ExtractionDetailResponse(
                result.id().toString(),
                result.sourceFileName(),
                result.extractedAt(),
                berufe,
                toVerificationResponse(result.verification())
        );
    }

    public BerufResponse toBerufResponse(Beruf beruf) {
        List<PruefungsBereichResponse> bereiche = beruf.pruefungsBereich().stream()
                .map(this::toBereichResponse)
                .toList();
        return new BerufResponse(beruf.beschreibung(), beruf.berufNr(), bereiche);
    }

    public VerificationResultResponse toVerificationResponse(VerificationResult verification) {
        if (verification == null) {
            return null;
        }
        List<VerificationIssueResponse> issues = verification.issues().stream()
                .map(i -> new VerificationIssueResponse(
                        i.severity().name(),
                        i.field(),
                        i.message()
                ))
                .toList();
        return new VerificationResultResponse(verification.valid(), issues, verification.verifiedAt());
    }

    private PruefungsBereichResponse toBereichResponse(PruefungsBereich bereich) {
        List<AufgabeResponse> aufgaben = bereich.aufgaben().stream()
                .map(this::toAufgabeResponse)
                .toList();
        return new PruefungsBereichResponse(bereich.name(), aufgaben);
    }

    private AufgabeResponse toAufgabeResponse(Aufgabe aufgabe) {
        Termin t = aufgabe.termin();
        TerminResponse termin = new TerminResponse(t.datum(), t.uhrzeitVon(), t.uhrzeitBis(), t.dauer());
        return new AufgabeResponse(aufgabe.name(), aufgabe.struktur(), termin, aufgabe.hilfmittel());
    }
}
