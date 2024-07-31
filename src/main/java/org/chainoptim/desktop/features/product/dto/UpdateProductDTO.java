package org.chainoptim.desktop.features.product.dto;

import org.chainoptim.desktop.features.product.model.NewUnitOfMeasurement;
import lombok.Data;

@Data
public class UpdateProductDTO {

    private Integer id;
    private String name;
    private String description;
    private Integer unitId;
    private NewUnitOfMeasurement newUnit;
}
