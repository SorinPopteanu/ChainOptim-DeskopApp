package org.chainoptim.desktop.features.goods.product.dto;

import org.chainoptim.desktop.features.goods.unit.model.UnitOfMeasurement;
import lombok.Data;

@Data
public class CreateProductDTO {

    private String name;
    private String description;
    private Integer organizationId;
    private UnitOfMeasurement newUnit;
}

