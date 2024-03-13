package org.chainoptim.desktop.features.factory.factorygraph.model;

import lombok.Data;

@Data
public class Node {
    SmallStage smallStage;
    Float numberOfStepsCapacity;
    Float perDuration;
    Integer priority;

}
