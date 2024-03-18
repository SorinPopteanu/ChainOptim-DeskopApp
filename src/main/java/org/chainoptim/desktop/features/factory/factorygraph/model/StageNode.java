package org.chainoptim.desktop.features.factory.factorygraph.model;

import lombok.Data;

@Data
public class StageNode {
    SmallStage smallStage;
    Float numberOfStepsCapacity;
    Float minimumRequiredCapacity;
    Float perDuration;
    Integer priority;
    Float allocationCapacityRatio;
}
