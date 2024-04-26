package org.chainoptim.desktop.features.product.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.shared.features.location.model.Location;

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
        this.stages = newProduct.getStages();
    }

    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private UnitOfMeasurement unit;
    private List<Stage> stages;
}
