package org.chainoptim.desktop.features.product.model;

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
