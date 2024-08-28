package org.chainoptim.desktop.features.production.inventory.dto;

import lombok.Data;

@Data
public class UpdateFactoryInventoryItemDTO {

    private Integer id;
    private Integer organizationId;
    private Integer factoryId;
    private Integer productId;
    private Integer componentId;
    private Float quantity;
    private Float minimumRequiredQuantity;
    private String companyId;
}
