package org.chainoptim.desktop.features.factory.model;

import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.productpipeline.model.Component;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryInventoryItem {

    private Integer id;

    private Integer factoryId;

    private Component component;

    private Product product;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Float quantity;

    private Float minimumRequiredQuantity;
}
