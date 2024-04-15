package org.chainoptim.desktop.shared.common.uielements.info;

import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.InfoLevel;

import java.util.EnumMap;

public class FeatureInfoMapper {

    private FeatureInfoMapper() {}

    private static final EnumMap<Feature, FeatureInfo> featureInfoMap = new EnumMap<>(Feature.class);

    static {
        featureInfoMap.put(Feature.PRODUCT, new FeatureInfo(
                "A Product is any item that is manufactured and/or sold by your organization.",
                InfoLevel.ADVANCED));
        featureInfoMap.put(Feature.PRODUCT_STAGE, new FeatureInfo(
                "A Product Stage is any step in the manufacturing process of a product. " +
                "You can define any number of Stage Inputs and Stage Outputs, consisting of " +
                "Components and how much of them are needed or produced in one full Stage. " +
                "You can also set up Connections between them, effectively configuring a 'Production Pipeline'.",
                InfoLevel.ADVANCED));
        featureInfoMap.put(Feature.COMPONENT, new FeatureInfo(
                "A .",
                InfoLevel.ALL));
    }

    public static FeatureInfo getFeatureInfo(Feature feature) {
        return featureInfoMap.getOrDefault(feature, new FeatureInfo("No information available", InfoLevel.NONE));
    }
}
