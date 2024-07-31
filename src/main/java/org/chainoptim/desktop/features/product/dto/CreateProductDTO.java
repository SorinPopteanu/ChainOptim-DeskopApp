package org.chainoptim.desktop.features.product.dto;

import org.chainoptim.desktop.features.product.model.NewUnitOfMeasurement;
import lombok.Data;

@Data
public class CreateProductDTO {

    private String name;
    private String description;
    private Integer organizationId;
    private Integer unitId;
    private CreateUnitOfMeasurementDTO unitDTO;
    private boolean createUnit;
    private NewUnitOfMeasurement newUnit;
}

