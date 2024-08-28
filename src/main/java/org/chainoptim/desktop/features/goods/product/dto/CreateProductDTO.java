package org.chainoptim.desktop.features.goods.product.dto;

import org.chainoptim.desktop.features.goods.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.goods.unit.model.NewUnitOfMeasurement;
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

