import * as d3 from "d3";
import { InteractionManager } from "./InteractionManager";
import { NodeRenderer } from "./NodeRenderer";
import { EdgeRenderer } from "./EdgeRenderer";
import { FactoryProductionGraph } from "./dataTypes";
import { assignPositionsToNodes, findStartingNodes } from "./graphProcessing";
import { highlightNode, unhighlightNodes } from "./renderGraph";

export class GraphRenderer {
    private nodeRenderer: NodeRenderer;
    private edgeRenderer: EdgeRenderer;
    private interactionManager: InteractionManager;
    private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>;


    constructor(containerId: string) {
        this.svg = d3.select(containerId).append("svg")
            .attr("width", 800)
            .attr("height", 600)

        this.nodeRenderer = new NodeRenderer(this.svg);
        this.edgeRenderer = new EdgeRenderer(this.svg);
    }

    renderGraph(graphData: FactoryProductionGraph) {
        // Preprocess graph: assign position to nodes based on connections
        const startingNodeIds = findStartingNodes(graphData.factoryGraph);
        const factoryGraphUI = assignPositionsToNodes(startingNodeIds, graphData.factoryGraph);

        this.setupSvgDefinitions();

        const stageInputNodeRadius = 14;

        // Draw all nodes
        Object.entries(factoryGraphUI.nodes).forEach(([stageNodeId, node]) => {
            this.nodeRenderer.renderGraphNode(node, parseInt(stageNodeId, 10), node.coordinates.x, node.coordinates.y, stageInputNodeRadius);
        });

        // Draw all edges
        Object.entries(factoryGraphUI.nodes).forEach(([stageNodeId, node]) => {
            this.edgeRenderer.renderEdges(node, factoryGraphUI.adjList, stageInputNodeRadius,  parseInt(stageNodeId, 10));
        });

        // Set up interactions and connect them to JavaFX via JavaConnector
        // this.interactionManager = new InteractionManager(this.svg, this.nodeRenderer);
        // this.interactionManager.setupNodeInteractions((nodeId) => {
        //     console.log("Node clicked:", nodeId);
        //     window.javaConnector.handleNodeClick(nodeId);
        // });
    }

    setupSvgDefinitions() {
        // Define arrowhead marker
        this.svg.append("defs").append("marker")
            .attr("id", "arrowhead")
            .attr("viewBox", "-0 -5 10 10")
            .attr("refX", 5)
            .attr("refY", 0)
            .attr("markerWidth", 5)
            .attr("markerHeight", 5)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M0,-5L10,0L0,5")
            .attr("fill", "#000");

        // Define drop shadow filter
        const defs = this.svg.append("defs");
        const filter = defs.append("filter")
            .attr("id", "drop-shadow")
            .attr("height", "130%");
        filter.append("feGaussianBlur")
            .attr("in", "SourceAlpha")
            .attr("stdDeviation", 3)
            .attr("result", "blur");
        filter.append("feOffset")
            .attr("in", "blur")
            .attr("dx", 1)
            .attr("dy", 2)
            .attr("result", "offsetBlur");
        const feMerge = filter.append("feMerge");
        feMerge.append("feMergeNode")
            .attr("in", "offsetBlur");
        feMerge.append("feMergeNode")
            .attr("in", "SourceGraphic");
    }
    
}