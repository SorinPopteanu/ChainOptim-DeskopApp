package org.chainoptim.desktop.features.production.analysis.factorygraph.model;

import lombok.Data;

@Data
public class FactoryStageNode {
    SmallStage smallStage;
    Float numberOfStepsCapacity;
    Float minimumRequiredCapacity;
    Float perDuration;
    Integer priority;
    Float allocationCapacityRatio;
}
