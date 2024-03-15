import { FactoryProductionGraph, StageNode } from "./dataTypes";

import * as d3 from "d3";
import { Coordinates, EdgeUI, StageNodeUI } from "./uiTypes";
import { assignPositionsToNodes, findStartingNodes } from "./graphProcessing";
import { encodeStageInputId, encodeStageOutputId } from "./utils";
import { calculateEdgePoints, getCirclePoint } from "./geometryUtils";
export {};

function renderGraph(jsonData: string) {
    const data: FactoryProductionGraph = JSON.parse(jsonData);

    // Preprocess graph: assign position to nodes based on connections
    const startingNodeIds = findStartingNodes(data.factoryGraph);
    const factoryGraphUI = assignPositionsToNodes(startingNodeIds, data.factoryGraph);

    const width = 800,
        height = 600;
    const stageInputNodeRadius = 10;

    // Create SVG container
    const svg = d3.select("#viz").append("svg").attr("width", width).attr("height", height);

    // Draw all nodes
    Object.entries(factoryGraphUI.nodes).forEach(([stageNodeId, node]) => {
        renderGraphNode(svg, node, parseInt(stageNodeId, 10), node.coordinates.x, node.coordinates.y, stageInputNodeRadius);
    });

    // Draw all edges
    Object.entries(factoryGraphUI.nodes).forEach(([stageNodeId, node]) => {
        renderEdges(svg, node, factoryGraphUI.adjList, stageInputNodeRadius,  parseInt(stageNodeId, 10));
    });
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
    const stageBoxWidth = 80;
    const stageBoxHeight = 50;
    const { x: stageBoxX, y: stageBoxY } = renderMainNode(svg, node, stageNodeId, centerX, centerY, stageBoxWidth, stageBoxHeight);

    // Add stage input and output subnodes
    const stageWidth = 100;
    const stageHeight = 120;

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

    svg.append("rect")
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

    svg.append("text")
        .attr("x", centerX)
        .attr("y", centerY)
        .attr("text-anchor", "middle")
        .attr("dominant-baseline", "central")
        .text(stageName)
        .style("fill", "black")
        .style("font-family", "Arial, sans-serif")
        .style("font-size", "12px");

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
            .attr("stroke-width", 1);
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
        console.log("NodeID: ", nodeId);

        svg.append("circle")
            .attr("id", nodeId)
            .attr("cx", stageOutputX)
            .attr("cy", stageOutputY)
            .style("fill", "#f0f0f0")
            .style("stroke", "gray")
            .style("stroke-width", 1)
            .attr("r", stageOutputNodeRadius);

        // Add edge from stage output to main stage box
        const edgeId = nodeId + ":edge:" + stageNodeId;
        svg.append("line")
            .attr("id", edgeId)
            .attr("x1", stageOutputX)
            .attr("y1", stageOutputY - stageOutputNodeRadius) // Connect from the top of the circle
            .attr("x2", centerX + stageOutputRelativeX / 5) // Connect to around the center X and bottm Y of the box
            .attr("y2", stageBoxY)
            .attr("stroke", "black")
            .attr("stroke-width", 1);
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
            .attr("stroke", "black")
            .attr("stroke-width", 1);
    });
};

window.renderGraph = renderGraph;
