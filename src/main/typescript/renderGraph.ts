import { FactoryProductionGraph, StageNode } from "./types";

import * as d3 from "d3";
export {};

function renderGraph(jsonData: string) {
    const data: FactoryProductionGraph = JSON.parse(jsonData);

    const width = 800,
        height = 600;

    // Create SVG container
    const svg = d3.select("#viz").append("svg").attr("width", width).attr("height", height);

    // Draw all nodes
    Object.values(data.factoryGraph.nodes).forEach((node, index) => {
        renderGraphNode(svg, node, index, 100 + index * 200, 100);
    });

    // const nodeElement = d3.select(`#${nodeId}`);
}

function renderGraphNode(
    svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
    node: StageNode,
    stageNodeId: number,
    centerX: number,
    centerY: number
) {
    // Render main stage box
    const { x: stageBoxX, y: stageBoxY } = renderMainNode(svg, node, stageNodeId, centerX, centerY);

    // Add stage input subnodes
    const stageWidth = 100;
    const stageHeight = 120;
    const stageInputNodeRadius = 10;

    renderStageInputs(svg, node, stageNodeId, centerX, centerY, stageWidth, stageHeight, stageInputNodeRadius, stageBoxY);
}

const renderMainNode = (
    svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
    node: StageNode,
    stageNodeId: number,
    centerX: number,
    centerY: number
): Coordinate => {
    const stageBoxWidth = 80;
    const stageBoxHeight = 50;
    const stageBoxX = centerX - stageBoxWidth / 2;
    const stageBoxY = centerY - stageBoxHeight / 2;
    const stageName = node.smallStage.stageName;

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
    node: StageNode,
    stageNodeId: number,
    centerX: number,
    centerY: number,
    stageWidth: number,
    stageHeight: number,
    stageInputNodeRadius: number,
    stageBoxY: number
) => {
    const stageInputs = node.smallStage.stageInputs;
    const numberOfInputs = stageInputs.length - 1;

    stageInputs.forEach((input, index) => {
        // Add stage input subnode
        const stageInputRelativeX =
            numberOfInputs > 0 ? (index - numberOfInputs / 2) * (stageWidth / numberOfInputs) : 0;
        const stageInputX = centerX + stageInputRelativeX;
        const stageInputY = centerY - stageHeight / 2;

        const nodeId: string = stageNodeId + ":si:" + input.id;

        svg.append("circle")
            .attr("id", nodeId)
            .attr("cx", stageInputX)
            .attr("cy", stageInputY)
            .style("fill", "#f0f0f0")
            .style("stroke", "gray")
            .style("stroke-width", 1)
            .attr("r", stageInputNodeRadius);

        // Add edge from stage input to main stage box
        const edgeId = nodeId + ":edge:" + stageNodeId;
        svg.append("line")
            .attr("id", edgeId)
            .attr("x1", stageInputX)
            .attr("y1", stageInputY + stageInputNodeRadius) // Connect from the bottom of the circle
            .attr("x2", centerX + stageInputRelativeX / 10) // Connect to around the center X and top Y of the box
            .attr("y2", stageBoxY)
            .attr("stroke", "black")
            .attr("stroke-width", 1);
    });
};

window.renderGraph = renderGraph;

export interface Coordinate {
    x: number;
    y: number;
}
