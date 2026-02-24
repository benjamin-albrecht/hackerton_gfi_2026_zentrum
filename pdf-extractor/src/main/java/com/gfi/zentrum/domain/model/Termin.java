package com.gfi.zentrum.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Termin(
        LocalDate datum,
        LocalTime uhrzeitVon,
        LocalTime uhrzeitBis,
        int dauer
) {
}
