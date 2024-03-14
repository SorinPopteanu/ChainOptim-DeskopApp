package org.chainoptim.desktop.features.factory.factorygraph.model;

import lombok.Data;

@Data
public class StageNode {
    SmallStage smallStage;
    Float numberOfStepsCapacity;
    Float perDuration;
    Integer priority;

}
