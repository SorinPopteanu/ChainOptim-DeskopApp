package org.chainoptim.desktop.features.product.model;

import lombok.Data;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.shared.features.location.model.Location;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Product {

    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private Integer unitId;
    private List<Stage> stages;
}
