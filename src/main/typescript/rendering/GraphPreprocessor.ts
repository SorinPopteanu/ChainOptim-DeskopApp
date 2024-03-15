import { GraphUIConfig } from "../config/GraphUIConfig";
import { FactoryGraph } from "../types/dataTypes";
import { FactoryGraphUI } from "../types/uiTypes";

export class GraphPreprocessor {
    constructor() {}

    
    public preprocessGraph = (factoryGraph: FactoryGraph): FactoryGraphUI => {
        const startingNodeIds = this.findStartingNodes(factoryGraph);
        const factoryGraphUI = this.assignPositionsToNodes(startingNodeIds, factoryGraph);
        return factoryGraphUI;
    }

    /*
    * Function for finding the nodes that have no incoming edges.
    */
    findStartingNodes = (factoryGraph: FactoryGraph): number[] => {
        const allNodeIds = new Set<number>(Object.keys(factoryGraph.nodes).map(Number));

        // Identify target nodes
        const targetNodeIds = new Set<number>();
        Object.values(factoryGraph.adjList).forEach((edges) => {
            edges.forEach((edge) => {
                targetNodeIds.add(edge.outgoingFactoryStageId);
            });
        });

        // Find starting nodes
        const startingNodeIds = Array.from(allNodeIds).filter((nodeId) => !targetNodeIds.has(nodeId));
        console.log("Starting nodes: ", startingNodeIds);

        return startingNodeIds;
    }

    /*
    * Function for transforming data types in UI types
    * and recursively assigning positions to nodes in the graph based on their edges.
    */
    assignPositionsToNodes(
        startingNodeIds: number[],
        factoryGraph: FactoryGraph
    ): FactoryGraphUI {
        // Transform to UI types
        const nodesUI = Object.fromEntries(
            Object.entries(factoryGraph.nodes).map(([nodeId, node]) => {
                return [
                    nodeId,
                    {
                        node: node,
                        coordinates: { x: 0, y: 0 },
                        visited: false,
                    },
                ];
            })
        );
        const adjListUI = Object.fromEntries(
            Object.entries(factoryGraph.adjList).map(([nodeId, edges]) => {
                return [
                    nodeId,
                    edges.map((edge) => {
                        return {
                            edge: edge,
                        };
                    }),
                ];
            })
        );
    
        let factoryGraphUI: FactoryGraphUI = {
            nodes: nodesUI,
            adjList: adjListUI,
            pipelinePriority: factoryGraph.pipelinePriority,
        };
    
        // Assign positions to nodes
        for (let i = 0; i < startingNodeIds.length; i++) {
            let depth = 0;
            factoryGraphUI = this.assignPositionsRecursively(startingNodeIds[i], i, factoryGraphUI, depth);
        }
    
        return factoryGraphUI;
    }
    
    assignPositionsRecursively(
        startingNodeId: number,
        startingNodeIndex: number,
        factoryGraph: FactoryGraphUI,
        depth: number,
    ): FactoryGraphUI {
        const { spaceBetweenStagesX, spaceBetweenStagesY, paddingX, paddingY } = GraphUIConfig.graph;

        factoryGraph.nodes[startingNodeId].coordinates = { 
            x: paddingX + startingNodeIndex * spaceBetweenStagesX, 
            y: paddingY + depth * spaceBetweenStagesY 
        };
        factoryGraph.nodes[startingNodeId].visited = true;
    
        let adjNodes = factoryGraph.adjList[startingNodeId];
    
        for (let j = 0; j < adjNodes.length; j++) {
            const targetNodeId = adjNodes[j].edge.outgoingFactoryStageId;
            if (!factoryGraph.nodes[targetNodeId].visited) {
                factoryGraph = this.assignPositionsRecursively(
                    targetNodeId,
                    startingNodeIndex,
                    factoryGraph,
                    depth + 1 // Just go down for now. In the future, find a middle point with siblings.
                );
            }
        }
    
        return factoryGraph;
    }    
}