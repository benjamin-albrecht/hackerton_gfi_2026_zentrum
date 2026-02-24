package com.gfi.zentrum.adapter.in.rest.dto;

public record VerificationIssueResponse(
        String severity,
        String field,
        String message
) {
}
