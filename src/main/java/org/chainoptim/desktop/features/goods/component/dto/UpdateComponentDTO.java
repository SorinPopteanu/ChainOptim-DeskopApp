package org.chainoptim.desktop.features.goods.component.dto;

import org.chainoptim.desktop.features.goods.unit.model.UnitOfMeasurement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateComponentDTO {

    private Integer id;
    private Integer organizationId;
    private String name;
    private String description;
    private Integer unitId;
    private UnitOfMeasurement newUnit;
}
