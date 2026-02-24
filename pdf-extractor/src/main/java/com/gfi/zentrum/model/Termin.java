package com.gfi.zentrum.model;

public class Termin {
    private String datum;
    private String uhrzeitvon;
    private String uhrzeitbis;
    private Integer dauer;

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getUhrzeitvon() {
        return uhrzeitvon;
    }

    public void setUhrzeitvon(String uhrzeitvon) {
        this.uhrzeitvon = uhrzeitvon;
    }

    public String getUhrzeitbis() {
        return uhrzeitbis;
    }

    public void setUhrzeitbis(String uhrzeitbis) {
        this.uhrzeitbis = uhrzeitbis;
    }

    public Integer getDauer() {
        return dauer;
    }

    public void setDauer(Integer dauer) {
        this.dauer = dauer;
    }
}
