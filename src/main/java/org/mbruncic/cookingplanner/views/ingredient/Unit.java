package org.mbruncic.cookingplanner.views.ingredient;

public enum Unit {
    gr("gr"),
    ml("ml"),
    kom("kom"),
    zlica("zlica"),
    gr_net("gr(neto)"),
    ;

    private final String name;

    Unit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
