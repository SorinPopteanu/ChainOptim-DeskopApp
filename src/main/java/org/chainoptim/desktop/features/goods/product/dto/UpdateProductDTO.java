package org.chainoptim.desktop.features.goods.product.dto;

import org.chainoptim.desktop.features.goods.unit.model.UnitOfMeasurement;
import lombok.Data;

@Data
public class UpdateProductDTO {

    private Integer id;
    private Integer organizationId;
    private String name;
    private String description;
    private Integer unitId;
    private UnitOfMeasurement newUnit;
}
