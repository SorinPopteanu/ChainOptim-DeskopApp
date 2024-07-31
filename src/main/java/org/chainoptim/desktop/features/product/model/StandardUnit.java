package org.chainoptim.desktop.features.product.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StandardUnit {

    METER("Meter", "m", "Length"),
    INCH("Inch", "in", "Length"),

    KILOGRAM("gram", "g", "Mass"),
    POUND("Pound", "lb", "Mass"),

    LITER("Liter", "l", "Volume");

    private final String name;
    private final String abbreviation;
    private final String category;

    @Override
    public String toString() {
        if (name.isEmpty())
            return name;
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
