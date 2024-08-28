package org.chainoptim.desktop.features.storage.inventory.model;

import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.features.goods.component.model.Component;

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

    public WarehouseInventoryItem(WarehouseInventoryItem warehouseInventoryItem) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.organizationId = organizationId;
        this.companyId = companyId;
        this.component = component;
        this.product = product;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.quantity = quantity;
        this.minimumRequiredQuantity = minimumRequiredQuantity;
    }

    private Integer id;
    private Integer warehouseId;
    private Integer organizationId;
    private String companyId;
    private Component component;
    private Product product;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float quantity;
    private Float minimumRequiredQuantity;

}
