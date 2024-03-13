package org.chainoptim.desktop.features.factory.factorygraph.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class FactoryGraph {
    private Map<Integer, StageNode> nodes = new HashMap<>(); // Key: factoryStageId
    private Map<Integer, List<Edge>> adjList = new HashMap<>(); // Key: factoryStageId
    private Float pipelinePriority;
}