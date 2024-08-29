package org.chainoptim.desktop.features.goods.pricing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pricing {

    private Integer id;
    private Integer productId;
    private ProductPricing productPricing;
}