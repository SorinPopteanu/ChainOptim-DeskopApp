import { FactoryProductionGraph, StageNode } from "./dataTypes";

import * as d3 from "d3";
import { Coordinates, EdgeUI, StageNodeUI } from "./uiTypes";
import { assignPositionsToNodes, findStartingNodes } from "./graphProcessing";
import { encodeStageInputId, encodeStageOutputId } from "./utils";
import { calculateEdgePoints, getCirclePoint } from "./geometryUtils";
import { GraphRenderer } from "./GraphRenderer";
export {};

function renderGraph(jsonData: string) {
    const data: FactoryProductionGraph = JSON.parse(jsonData);

    const graphRenderer = new GraphRenderer("#viz");
    graphRenderer.renderGraph(data);
    // // Preprocess graph: assign position to nodes based on connections
    // const startingNodeIds = findStartingNodes(data.factoryGraph);
    // const factoryGraphUI = assignPositionsToNodes(startingNodeIds, data.factoryGraph);

    // const width = 800,
    //     height = 600;
    // const stageInputNodeRadius = 14;

    // // Create SVG container
    // const svg = d3.select("#viz").append("svg").attr("width", width).attr("height", height);

    // svg.append("defs").append("marker")
    //     .attr("id", "arrowhead")
    //     .attr("viewBox", "-0 -5 10 10")
    //     .attr("refX", 5)
    //     .attr("refY", 0)
    //     .attr("markerWidth", 5)
    //     .attr("markerHeight", 5)
    //     .attr("orient", "auto")
    //     .append("path")
    //     .attr("d", "M0,-5L10,0L0,5")
    //     .attr("fill", "#000");

    // // Draw all nodes
    // Object.entries(factoryGraphUI.nodes).forEach(([stageNodeId, node]) => {
    //     renderGraphNode(svg, node, parseInt(stageNodeId, 10), node.coordinates.x, node.coordinates.y, stageInputNodeRadius);
    // });

    // // Draw all edges
    // Object.entries(factoryGraphUI.nodes).forEach(([stageNodeId, node]) => {
    //     renderEdges(svg, node, factoryGraphUI.adjList, stageInputNodeRadius,  parseInt(stageNodeId, 10));
    // });
}

function renderGraphNode(
    svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
    node: StageNodeUI,
    stageNodeId: number,
    centerX: number,
    centerY: number,
    circleRadius: number
) {
    // Render main stage box
    const stageBoxWidth = 90;
    const stageBoxHeight = 60;
    const { x: stageBoxX, y: stageBoxY } = renderMainNode(svg, node, stageNodeId, centerX, centerY, stageBoxWidth, stageBoxHeight);

    // Add stage input and output subnodes
    const stageWidth = 100;
    const stageHeight = 140;

    renderStageInputs(svg, node, stageNodeId, centerX, centerY, stageWidth, stageHeight, circleRadius, stageBoxY);
    renderStageOutputs(svg, node, stageNodeId, centerX, centerY, stageWidth, stageHeight, circleRadius, stageBoxY + stageBoxHeight);
}

const renderMainNode = (
    svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
    nodeUI: StageNodeUI,
    stageNodeId: number,
    centerX: number,
    centerY: number,
    stageBoxWidth: number,
    stageBoxHeight: number
): Coordinates => {
    const stageBoxX = centerX - stageBoxWidth / 2;
    const stageBoxY = centerY - stageBoxHeight / 2;
    const stageName = nodeUI.node.smallStage.stageName;

    // Define drop shadow filter
    const defs = svg.append("defs");
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
        .attr("in", "offsetBlur")
    feMerge.append("feMergeNode")
        .attr("in", "SourceGraphic");

    // Draw the node
    const node = svg.append("rect")
        .attr("id", stageNodeId)
        .attr("x", stageBoxX)
        .attr("y", stageBoxY)
        .attr("width", stageBoxWidth)
        .attr("height", stageBoxHeight)
        .style("fill", "#f0f0f0")
        .style("stroke", "gray")
        .style("stroke-width", 1)
        .attr("rx", 4)
        .attr("ry", 4);

    // Add node click event listener
    node.on("click", function(event) {
        unhighlightNodes(svg);
        highlightNode(d3.select(this));

        const clickedNodeId = this.id;
        window.javaConnector.handleNodeClick(clickedNodeId);
    });

    svg.append("text")
        .attr("x", centerX)
        .attr("y", centerY)
        .attr("text-anchor", "middle")
        .attr("dominant-baseline", "central")
        .text(stageName)
        .style("fill", "black")
        .style("font-family", "Arial, sans-serif")
        .style("font-size", "12px")
        .style("pointer-events", "none");

    return { x: stageBoxX, y: stageBoxY };
};

const renderStageInputs = (
    svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
    nodeUI: StageNodeUI,
    stageNodeId: number,
    centerX: number,
    centerY: number,
    stageWidth: number,
    stageHeight: number,
    stageInputNodeRadius: number,
    stageBoxY: number
) => {
    const stageInputs = nodeUI.node.smallStage.stageInputs;
    const numberOfInputs = stageInputs.length - 1;

    stageInputs.forEach((input, index) => {
        // Add stage input subnode
        const stageInputRelativeX =
            numberOfInputs > 0 ? (index - numberOfInputs / 2) * (stageWidth / numberOfInputs) : 0;
        const stageInputX = centerX + stageInputRelativeX;
        const stageInputY = centerY - stageHeight / 2;

        const nodeId: string = encodeStageInputId(stageNodeId, input.id);
        console.log("NodeID: ", nodeId);

        svg.append("circle")
            .attr("id", nodeId)
            .attr("cx", stageInputX)
            .attr("cy", stageInputY)
            .style("fill", "#f0f0f0")
            .style("stroke", "gray")
            .style("stroke-width", 1)
            .attr("r", stageInputNodeRadius);

        svg.append("text")
            .attr("x", stageInputX)
            .attr("y", stageInputY)
            .attr("text-anchor", "middle")
            .attr("dominant-baseline", "central")
            .text(input.componentId)
            .style("fill", "black")
            .style("font-family", "Arial, sans-serif")
            .style("font-size", "10px");

        // Add edge from stage input to main stage box
        const edgeId = nodeId + "_edge_" + stageNodeId;

        const stageInputConnectingCoordinates = getCirclePoint(stageInputX, stageInputY + stageInputNodeRadius, stageInputNodeRadius, 0.25);

        svg.append("line")
            .attr("id", edgeId)
            .attr("x1", stageInputConnectingCoordinates.x)
            .attr("y1", stageInputConnectingCoordinates.y) // Connect from the bottom of the circle
            .attr("x2", centerX + stageInputRelativeX / 5) // Connect to around the center X and top Y of the box
            .attr("y2", stageBoxY)
            .attr("stroke", "black")
            .attr("stroke-width", 1)
            .attr("marker-end", "url(#arrowhead)");
    });
};

const renderStageOutputs = (
    svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
    nodeUI: StageNodeUI,
    stageNodeId: number,
    centerX: number,
    centerY: number,
    stageWidth: number,
    stageHeight: number,
    stageOutputNodeRadius: number,
    stageBoxY: number
) => {
    const stageOutputs = nodeUI.node.smallStage.stageOutputs;
    const numberOfOutputs = stageOutputs.length - 1;

    stageOutputs.forEach((output, index) => {
        // Add stage output subnode
        const stageOutputRelativeX =
            numberOfOutputs > 0 ? (index - numberOfOutputs / 2) * (stageWidth / numberOfOutputs) : 0;
        const stageOutputX = centerX + stageOutputRelativeX;
        const stageOutputY = centerY + stageHeight / 2;

        const nodeId: string = encodeStageOutputId(stageNodeId, output.id);

        svg.append("circle")
            .attr("id", nodeId)
            .attr("cx", stageOutputX)
            .attr("cy", stageOutputY)
            .style("fill", "#f0f0f0")
            .style("stroke", "gray")
            .style("stroke-width", 1)
            .attr("r", stageOutputNodeRadius);

        svg.append("text")
            .attr("x", stageOutputX)
            .attr("y", stageOutputY)
            .attr("text-anchor", "middle")
            .attr("dominant-baseline", "central")
            .text(output.componentId)
            .style("fill", "black")
            .style("font-family", "Arial, sans-serif")
            .style("font-size", "10px");

        // Add edge from stage output to main stage box
        const edgeId = nodeId + ":edge:" + stageNodeId;

        const stageOutputConnectingCoordinates = getCirclePoint(stageOutputX, stageOutputY + stageOutputNodeRadius, stageOutputNodeRadius, 0.75);

        svg.append("line")
            .attr("id", edgeId)
            .attr("x1", centerX + stageOutputRelativeX / 5) // Connect to around the center X and bottm Y of the box
            .attr("y1", stageBoxY) // Connect from the top of the circle
            .attr("x2", stageOutputConnectingCoordinates.x)
            .attr("y2", stageOutputConnectingCoordinates.y)
            .attr("stroke", "black")
            .attr("stroke-width", 1)
            .attr("marker-end", "url(#arrowhead)");
    });
};

const renderEdges = (
    svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
    nodeUI: StageNodeUI,
    adjListUI: Record<number, EdgeUI[]>,
    circleRadius: number,
    stageNodeId: number
) => {
    const neighbors = adjListUI[stageNodeId];
    if (!neighbors) {
        return;
    }

    neighbors.forEach((neighbor) => {
        // Find incoming stage output and outgoing stage input
        const sourceElement = d3.select(
            `#${encodeStageOutputId(neighbor.edge.incomingFactoryStageId, neighbor.edge.incomingStageOutputId)}`
        );
        const targetElement = d3.select(
            `#${encodeStageInputId(neighbor.edge.outgoingFactoryStageId, neighbor.edge.outgoingStageInputId)}`
        );

        if (sourceElement.empty() || targetElement.empty()) {
            console.log(
                "Source or target element empty for: ",
                encodeStageOutputId(neighbor.edge.incomingFactoryStageId, neighbor.edge.incomingStageOutputId),
                encodeStageInputId(neighbor.edge.outgoingFactoryStageId, neighbor.edge.outgoingStageInputId)
            );
            return;
        }

        const { start, end } = calculateEdgePoints(
            { x: parseFloat(sourceElement.attr("cx")), y: parseFloat(sourceElement.attr("cy")) },
            { x: parseFloat(targetElement.attr("cx")), y: parseFloat(targetElement.attr("cy")) },
            circleRadius,
            circleRadius
        );

        svg.append("line")
            .attr("x1", start.x)
            .attr("y1", start.y)
            .attr("x2", end.x)
            .attr("y2", end.y)
            .attr("stroke", "blue")
            .attr("stroke-width", 1)
            .attr("marker-end", "url(#arrowhead)");
    });
};

export const highlightNode = (node: d3.Selection<d3.BaseType, unknown, HTMLElement, any>) => {
    node
        .transition()
        .duration(150)
        .style("fill", "#d9e2ef")
        .style("stroke-width", 3)
        .attr("filter", "url(#drop-shadow)");
};

export const unhighlightNodes = (svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) => {
    svg.selectAll("rect")
        .transition()
        .duration(150)
        .style("fill", "#f0f0f0")
        .style("stroke-width", 1)
        .attr("filter", null);
};


window.renderGraph = renderGraph;
