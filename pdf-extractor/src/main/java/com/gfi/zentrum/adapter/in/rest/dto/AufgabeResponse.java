package com.gfi.zentrum.adapter.in.rest.dto;

public record AufgabeResponse(
        String name,
        String struktur,
        TerminResponse termin,
        String hilfmittel
) {
}
