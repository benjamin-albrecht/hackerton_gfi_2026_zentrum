package com.gfi.zentrum.adapter.in.rest.dto;

import java.util.List;

public record PruefungsBereichResponse(
        String name,
        List<AufgabeResponse> aufgaben
) {
}
