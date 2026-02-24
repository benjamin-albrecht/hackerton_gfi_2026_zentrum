package com.gfi.zentrum.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;

public record ExtractionDetailResponse(
        String id,
        String sourceFileName,
        Instant extractedAt,
        List<BerufResponse> berufe,
        VerificationResultResponse verification
) {
}
