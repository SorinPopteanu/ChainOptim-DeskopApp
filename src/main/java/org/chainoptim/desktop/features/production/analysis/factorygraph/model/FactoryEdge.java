package org.chainoptim.desktop.features.production.analysis.factorygraph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FactoryEdge {
    Integer incomingFactoryStageId;
    Integer incomingStageOutputId;
    Integer outgoingFactoryStageId;
    Integer outgoingStageInputId;
}
