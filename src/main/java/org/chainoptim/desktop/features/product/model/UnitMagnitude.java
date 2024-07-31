package org.chainoptim.desktop.features.product.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UnitMagnitude {

    MILLI("Milli", "m", 0.001f),
    CENTI("Centi", "c", 0.01f),
    DECI("Deci", "d", 0.1f),
    BASE("Base", "", 1f),
    DECA("Deca", "da", 10f),
    HECTO("Hecto", "h", 100f),
    KILO("Kilo", "k", 1000f);

    private final String name;
    private final String abbreviation;
    private final float magnitude;

    @Override
    public String toString() {
        if (name.isEmpty())
            return name;
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}