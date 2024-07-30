package org.chainoptim.desktop.features.product.dto;

import org.chainoptim.desktop.features.product.model.ProductPricing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePricingDTO {

    private Integer productId;
    private ProductPricing productPricing;

}
