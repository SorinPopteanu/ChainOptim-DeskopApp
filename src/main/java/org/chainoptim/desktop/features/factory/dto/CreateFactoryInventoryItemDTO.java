package org.chainoptim.desktop.features.factory.dto;

import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFactoryInventoryItemDTO {

    private Integer factoryId;
    private Integer organizationId;
    private Integer productId;
    private Integer componentId;
    private Float quantity;
    private Float minimumRequiredQuantity;
    private String companyId;
}
