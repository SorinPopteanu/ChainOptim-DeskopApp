package org.chainoptim.desktop.features.goods.pricing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPricing {

    private Float pricePerUnit;
    private TreeMap<Float, Float> pricePerVolume;
}
