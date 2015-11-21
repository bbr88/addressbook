package com.vaadin.tutorial.dmdproject.dbms;

/**
 * Created by bbr on 21.11.15.
 */
public enum Lenny {
    FACE("(͡° ͜ʖ͡°)");

    private String value;

    public String getValue() {
        return value;
    }

    Lenny(String value) {
        this.value = value;
    }
}
