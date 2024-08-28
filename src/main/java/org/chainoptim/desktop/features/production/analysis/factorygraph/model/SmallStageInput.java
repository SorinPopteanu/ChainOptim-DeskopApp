package org.chainoptim.desktop.features.production.analysis.factorygraph.model;

import lombok.Data;

@Data
public class SmallStageInput {
    Integer id;
    Integer componentId;
    Float quantityPerStage;
    Float allocatedQuantity;
    Float requestedQuantity;
}
