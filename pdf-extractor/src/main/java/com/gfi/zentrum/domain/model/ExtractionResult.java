package com.gfi.zentrum.domain.model;

import java.time.Instant;
import java.util.List;

public record ExtractionResult(
        ExtractionId id,
        String sourceFileName,
        Instant extractedAt,
        List<Beruf> berufe
) {
}
