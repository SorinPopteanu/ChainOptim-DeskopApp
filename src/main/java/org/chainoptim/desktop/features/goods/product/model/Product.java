package org.chainoptim.desktop.features.goods.product.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.features.goods.dto.UnitOfMeasurement;
import org.chainoptim.desktop.features.goods.unit.model.NewUnitOfMeasurement;
import org.chainoptim.desktop.features.goods.stage.model.Stage;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    public Product(Product newProduct) {
        this.id = newProduct.getId();
        this.name = newProduct.getName();
        this.description = newProduct.getDescription();
        this.createdAt = newProduct.getCreatedAt();
        this.updatedAt = newProduct.getUpdatedAt();
        this.organizationId = newProduct.getOrganizationId();
        this.unit = newProduct.getUnit();
        this.newUnit = newProduct.getNewUnit();
        this.stages = newProduct.getStages();
    }

    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private UnitOfMeasurement unit;
    private NewUnitOfMeasurement newUnit;
    private List<Stage> stages;
}
