package org.chainoptim.desktop.features.goods.productgraph.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.features.production.analysis.factorygraph.model.SmallStage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductGraph {
    private Map<Integer, SmallStage> nodes = new HashMap<>(); // Key: stageId
    private Map<Integer, List<ProductEdge>> adjList = new HashMap<>(); // Key: stageId
}
