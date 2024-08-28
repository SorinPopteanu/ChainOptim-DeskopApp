package org.chainoptim.desktop.features.production.model;

import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.features.goods.component.model.Component;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryInventoryItem {

    private Integer id;
    private Integer factoryId;
    private String companyId;
    private Component component;
    private Product product;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float quantity;
    private Float minimumRequiredQuantity;

    public FactoryInventoryItem(FactoryInventoryItem nweFactoryInventoryItem) {
        this.id = nweFactoryInventoryItem.getId();
        this.factoryId = nweFactoryInventoryItem.getFactoryId();
        this.companyId = nweFactoryInventoryItem.getCompanyId();
        this.component = nweFactoryInventoryItem.getComponent();
        this.product = nweFactoryInventoryItem.getProduct();
        this.createdAt = nweFactoryInventoryItem.getCreatedAt();
        this.updatedAt = nweFactoryInventoryItem.getUpdatedAt();
        this.quantity = nweFactoryInventoryItem.getQuantity();
        this.minimumRequiredQuantity = nweFactoryInventoryItem.getMinimumRequiredQuantity();
    }
}
