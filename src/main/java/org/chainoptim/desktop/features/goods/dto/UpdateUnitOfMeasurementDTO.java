package org.chainoptim.desktop.features.goods.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUnitOfMeasurementDTO {

    private Integer id;
    private String name;
//    private String abbreviation;

    private String unitType;
}

