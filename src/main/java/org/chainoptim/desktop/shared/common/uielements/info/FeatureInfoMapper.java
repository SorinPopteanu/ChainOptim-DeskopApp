package org.chainoptim.desktop.shared.common.uielements.info;

import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.InfoLevel;

import lombok.Getter;

import java.util.EnumMap;

public class FeatureInfoMapper {

    private FeatureInfoMapper() {}

    private static final EnumMap<Feature, FeatureInfo> featureInfoMap = new EnumMap<>(Feature.class);

    static {
        featureInfoMap.put(Feature.PRODUCT, new FeatureInfo(
                "A Product is any item that is manufactured and/or sold by your organization.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.PRODUCT_STAGE, new FeatureInfo(
                "A Product Stage is a step in the manufacturing process of a product.",
                InfoLevel.ADVANCED));
    }

    public static FeatureInfo getFeatureInfo(Feature feature) {
        return featureInfoMap.getOrDefault(feature, new FeatureInfo("No information available", InfoLevel.NONE));
    }
}
