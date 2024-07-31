package org.chainoptim.desktop.shared.util;

import org.chainoptim.desktop.features.product.model.NewUnitOfMeasurement;
import org.chainoptim.desktop.features.product.model.StandardUnit;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class UnitConversionUtil {

    private UnitConversionUtil() {}

    private static final Map<Pair<StandardUnit, StandardUnit>, Double> conversionMap = new HashMap<>();

    static {
        conversionMap.put(new Pair<>(StandardUnit.METER, StandardUnit.INCH), 39.3701);
        conversionMap.put(new Pair<>(StandardUnit.KILOGRAM, StandardUnit.POUND), 2.20462);
    }

    public static double getConversionFactor(StandardUnit from, StandardUnit to) {
        return conversionMap.getOrDefault(new Pair<>(from, to), Double.NaN);
    }

    public static double convert(double value, NewUnitOfMeasurement from, NewUnitOfMeasurement to) {
        if (!from.getStandardUnit().getCategory().equals(to.getStandardUnit().getCategory())) {
            throw new IllegalArgumentException("Cannot convert between different categories of units");
        }

        double baseValue = value * from.getUnitMagnitude().getMagnitude();

        double convertedValue = baseValue * getConversionFactor(from.getStandardUnit(), to.getStandardUnit());

        return convertedValue / to.getUnitMagnitude().getMagnitude();
    }
}
