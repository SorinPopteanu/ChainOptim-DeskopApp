package org.chainoptim.desktop.features.scanalysis.productgraph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductEdge {
    Integer incomingStageId;
    Integer incomingStageOutputId;
    Integer outgoingStageId;
    Integer outgoingStageInputId;
}
