package com.gfi.zentrum.domain.model;

public record VerificationIssue(
        Severity severity,
        String field,
        String message
) {
}
