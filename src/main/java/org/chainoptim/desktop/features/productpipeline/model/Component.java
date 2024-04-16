package org.chainoptim.desktop.features.productpipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.features.product.model.UnitOfMeasurement;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Component {

    public Component(Component data) {
        this.id = data.id;
        this.name = data.name;
        this.description = data.description;
        this.createdAt = data.createdAt;
        this.updatedAt = data.updatedAt;
        this.organizationId = data.organizationId;
        this.unit = data.unit;
    }

    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private UnitOfMeasurement unit;
}
