package com.gfi.zentrum.domain.model;

public class ExtractionNotFoundException extends RuntimeException {

    public ExtractionNotFoundException(ExtractionId id) {
        super("Extraction not found: " + id);
    }
}
