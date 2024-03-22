package org.chainoptim.desktop.features.product.dto;

import lombok.Data;

@Data
public class CreateProductDTO {

    private String name;
    private String description;
    private Integer organizationId;
    private Integer unitId;
    private CreateUnitOfMeasurementDTO unitDTO;
    private boolean createUnit;
}

