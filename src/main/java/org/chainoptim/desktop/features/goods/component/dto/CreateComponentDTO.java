package org.chainoptim.desktop.features.goods.component.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.features.goods.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.goods.unit.model.NewUnitOfMeasurement;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateComponentDTO {

    private String name;
    private String description;
    private Integer organizationId;
    private Integer unitId;
    private CreateUnitOfMeasurementDTO unitDTO;
    private boolean createUnit;
    private NewUnitOfMeasurement newUnit;
}
