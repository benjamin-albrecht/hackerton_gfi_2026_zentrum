package com.gfi.zentrum.adapter.in.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TerminResponse(
        LocalDate datum,
        LocalTime uhrzeitVon,
        LocalTime uhrzeitBis,
        int dauer
) {
}
