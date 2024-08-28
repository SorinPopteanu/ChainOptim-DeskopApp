package org.chainoptim.desktop.features.goods.product.dto;

import org.chainoptim.desktop.features.goods.unit.model.NewUnitOfMeasurement;
import lombok.Data;

@Data
public class UpdateProductDTO {

    private Integer id;
    private Integer organizationId;
    private String name;
    private String description;
    private Integer unitId;
    private NewUnitOfMeasurement newUnit;
}
