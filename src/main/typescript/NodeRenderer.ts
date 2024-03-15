import * as d3 from "d3";
import { Coordinates, StageNodeUI } from "./uiTypes";
import { highlightNode, unhighlightNodes } from "./renderGraph";
import { encodeStageInputId, encodeStageOutputId } from "./utils";
import { getCirclePoint } from "./geometryUtils";

export class NodeRenderer {
    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) {}

    
    public renderGraphNode = (
        node: StageNodeUI,
        stageNodeId: number,
        centerX: number,
        centerY: number,
        circleRadius: number
    ) => {
        // Render main stage box
        const stageBoxWidth = 90;
        const stageBoxHeight = 60;
        const { x: stageBoxX, y: stageBoxY } = this.renderMainNode(node, stageNodeId, centerX, centerY, stageBoxWidth, stageBoxHeight);

        // Add stage input and output subnodes
        const stageWidth = 100;
        const stageHeight = 140;

        this.renderStageInputs(node, stageNodeId, centerX, centerY, stageWidth, stageHeight, circleRadius, stageBoxY);
        this.renderStageOutputs(node, stageNodeId, centerX, centerY, stageWidth, stageHeight, circleRadius, stageBoxY + stageBoxHeight);
    }


    renderMainNode = (
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

        // Draw the node
        const node = this.svg.append("rect")
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
        node.on("click", (event) => {
            const clickedNode = d3.select(event.currentTarget);
            const clickedNodeId = clickedNode.attr("id");
            unhighlightNodes(this.svg);
            highlightNode(clickedNode);

            window.javaConnector.handleNodeClick(clickedNodeId);
        });

        this.svg.append("text")
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

    renderStageInputs = (
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

            this.svg.append("circle")
                .attr("id", nodeId)
                .attr("cx", stageInputX)
                .attr("cy", stageInputY)
                .style("fill", "#f0f0f0")
                .style("stroke", "gray")
                .style("stroke-width", 1)
                .attr("r", stageInputNodeRadius);

            this.svg.append("text")
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

            this.svg.append("line")
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

    renderStageOutputs = (
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

            this.svg.append("circle")
                .attr("id", nodeId)
                .attr("cx", stageOutputX)
                .attr("cy", stageOutputY)
                .style("fill", "#f0f0f0")
                .style("stroke", "gray")
                .style("stroke-width", 1)
                .attr("r", stageOutputNodeRadius);

            this.svg.append("text")
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

            this.svg.append("line")
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

    public highlightNode = (nodeId: string) => {
        const node = this.svg.select(`#${nodeId}`);
        node
            .transition()
            .duration(150)
            .style("fill", "#d9e2ef")
            .style("stroke-width", 3)
            .attr("filter", "url(#drop-shadow)");
    };
    
    public unhighlightAllNodes = () => {
        this.svg.selectAll("rect")
            .transition()
            .duration(150)
            .style("fill", "#f0f0f0")
            .style("stroke-width", 1)
            .attr("filter", null);
    };
}