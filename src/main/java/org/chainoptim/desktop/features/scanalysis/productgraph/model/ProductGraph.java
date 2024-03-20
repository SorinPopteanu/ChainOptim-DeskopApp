package org.chainoptim.desktop.features.scanalysis.productgraph.model;

import org.chainoptim.desktop.features.scanalysis.factorygraph.model.SmallStage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductGraph {
    private Map<Integer, SmallStage> nodes = new HashMap<>(); // Key: stageId
    private Map<Integer, List<ProductEdge>> adjList = new HashMap<>(); // Key: stageId
}
