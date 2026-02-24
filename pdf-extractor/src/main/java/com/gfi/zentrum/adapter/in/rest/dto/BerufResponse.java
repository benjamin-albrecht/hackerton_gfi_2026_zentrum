package com.gfi.zentrum.adapter.in.rest.dto;

import java.util.List;

public record BerufResponse(
        String beschreibung,
        List<Integer> berufNr,
        List<PruefungsBereichResponse> pruefungsBereich
) {
}
