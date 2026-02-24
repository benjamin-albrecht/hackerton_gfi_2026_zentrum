package com.gfi.zentrum.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ExtractionId(UUID value) {

    public ExtractionId {
        Objects.requireNonNull(value, "ExtractionId value must not be null");
    }

    public static ExtractionId generate() {
        return new ExtractionId(UUID.randomUUID());
    }

    public static ExtractionId of(String id) {
        return new ExtractionId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
