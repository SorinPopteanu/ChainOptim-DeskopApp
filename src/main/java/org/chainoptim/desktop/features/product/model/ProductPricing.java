package org.chainoptim.desktop.features.product.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPricing {

    private Float pricePerUnit;
    private TreeMap<Float, Float> pricePerVolume;
}
