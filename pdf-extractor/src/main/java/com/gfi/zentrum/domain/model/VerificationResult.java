package com.gfi.zentrum.domain.model;

import java.time.Instant;
import java.util.List;

public record VerificationResult(
        boolean valid,
        List<VerificationIssue> issues,
        Instant verifiedAt
) {
}
