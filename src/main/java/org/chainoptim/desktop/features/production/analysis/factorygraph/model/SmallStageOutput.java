package org.chainoptim.desktop.features.production.analysis.factorygraph.model;

import lombok.Data;

@Data
public class SmallStageOutput {
    Integer id;
    Integer componentId;
    Float quantityPerStage;
    Float expectedOutputPerAllocation;
    Float outputPerRequest;
}