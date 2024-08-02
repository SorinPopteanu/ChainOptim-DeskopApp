package org.chainoptim.desktop.features.warehouse.dto;

import lombok.Data;

@Data
public class UpdateWarehouseInventoryItemDTO {

    private Integer id;
    private Integer organizationId;
    private Integer warehouseId;
    private Integer productId;
    private Integer componentId;
    private Float quantity;
    private Float minimumRequiredQuantity;
    private String companyId;
}
