package org.chainoptim.desktop.features.warehouse.model;

import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.productpipeline.model.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseInventoryItem {

    private Integer id;
    private Integer warehouseId;
    private Integer organizationId;
    private Component component;
    private Product product;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float quantity;
    private Float minimumRequiredQuantity;

}
