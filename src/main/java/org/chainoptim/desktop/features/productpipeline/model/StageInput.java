package org.chainoptim.desktop.features.productpipeline.model;

import lombok.*;

@Data
public class StageInput {

    private Integer id;
    private Integer materialId;
    private Integer componentId;
    private Float quantity;
}
