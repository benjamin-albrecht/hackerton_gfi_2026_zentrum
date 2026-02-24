package com.gfi.zentrum.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionIdTest {

    @Test
    void generateCreatesUniqueIds() {
        ExtractionId id1 = ExtractionId.generate();
        ExtractionId id2 = ExtractionId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void ofParsesUuidString() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        ExtractionId id = ExtractionId.of(uuid);
        assertEquals(UUID.fromString(uuid), id.value());
        assertEquals(uuid, id.toString());
    }

    @Test
    void rejectsNullValue() {
        assertThrows(NullPointerException.class, () -> new ExtractionId(null));
    }

    @Test
    void ofRejectsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> ExtractionId.of("not-a-uuid"));
    }
}
