package com.gfi.zentrum.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;

public record VerificationResultResponse(
        boolean valid,
        List<VerificationIssueResponse> issues,
        Instant verifiedAt
) {
}
