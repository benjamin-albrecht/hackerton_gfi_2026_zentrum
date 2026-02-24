package com.gfi.zentrum.model;

public class Root {
    private Beruf beruf;

    public Root() {
    }

    public Root(Beruf beruf) {
        this.beruf = beruf;
    }

    public Beruf getBeruf() {
        return beruf;
    }

    public void setBeruf(Beruf beruf) {
        this.beruf = beruf;
    }
}
