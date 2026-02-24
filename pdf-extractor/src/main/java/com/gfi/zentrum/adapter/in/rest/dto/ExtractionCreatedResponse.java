package com.gfi.zentrum.adapter.in.rest.dto;

import java.time.Instant;

public record ExtractionCreatedResponse(
        String id,
        String sourceFileName,
        Instant extractedAt,
        int berufeCount
) {
}
