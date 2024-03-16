import * as d3 from "d3";
import { Coordinates, StageNodeUI } from "../types/uiTypes";
import { getCirclePoint } from "../utils/geometryUtils";
import { GraphUIConfig } from "../config/GraphUIConfig";
import { ElementIdentifier } from "../utils/ElementIdentifier";
import { findStageInputPosition, findStageOutputPosition } from "../utils/utils";

export class NodeRenderer {
    private elementIdentifier: ElementIdentifier;

    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) {
        this.elementIdentifier = new ElementIdentifier();
    }

    
    public renderGraphNode = (
        node: StageNodeUI,
        stageNodeId: number,
        centerX: number,
        centerY: number,
    ) => {
        const { stageWidth, stageHeight, stageBoxWidth, stageBoxHeight, subnodeRadius } = GraphUIConfig.node;

        // Render main stage box
        const { x: stageBoxX, y: stageBoxY } = this.renderMainNode(node, stageNodeId, centerX, centerY, stageBoxWidth, stageBoxHeight);

        // Add stage input and output subnodes
        this.renderStageInputs(node, stageNodeId, centerX, centerY, stageWidth, stageHeight, subnodeRadius, stageBoxY);
        this.renderStageOutputs(node, stageNodeId, centerX, centerY, stageWidth, stageHeight, subnodeRadius, stageBoxY + stageBoxHeight);
    }

    renderMainNode = (
        nodeUI: StageNodeUI,
        stageNodeId: number,
        centerX: number,
        centerY: number,
        stageBoxWidth: number,
        stageBoxHeight: number
    ): Coordinates => {
        const { backgroundColor, borderColor, borderRadius, borderWidth, fontColor, mainNodeFontSize } = GraphUIConfig.node;

        const stageBoxX = centerX - stageBoxWidth / 2;
        const stageBoxY = centerY - stageBoxHeight / 2;
        const stageName = nodeUI.node.smallStage.stageName;

        // Draw the node
        this.svg.append("rect")
            .attr("id", stageNodeId)
            .attr("x", stageBoxX)
            .attr("y", stageBoxY)
            .attr("width", stageBoxWidth)
            .attr("height", stageBoxHeight)
            .style("fill", backgroundColor)
            .style("stroke", borderColor)
            .style("stroke-width", borderWidth)
            .attr("rx", borderRadius)
            .attr("ry", borderRadius);

        this.svg.append("text")
            .attr("x", centerX)
            .attr("y", centerY)
            .attr("text-anchor", "middle")
            .attr("dominant-baseline", "central")
            .text(stageName)
            .style("fill", fontColor)
            .style("font-family", "Arial, sans-serif")
            .style("font-size", mainNodeFontSize)
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
        const { backgroundColor, borderColor, borderWidth, fontColor, subnodeFontSize, subedgeColor, subedgeWidth } = GraphUIConfig.node;

        const stageInputs = nodeUI.node.smallStage.stageInputs;
        const numberOfInputs = stageInputs.length - 1;
        
        stageInputs.forEach((input, index) => {
            // Add stage input subnode
            const stageInputRelativeX =
                numberOfInputs > 0 ? (index - numberOfInputs / 2) * (stageWidth / numberOfInputs) : 0;
            const { x: stageInputX, y: stageInputY } = findStageInputPosition(centerX, centerY, numberOfInputs, index);

            const nodeId: string = this.elementIdentifier.encodeStageInputId(stageNodeId, input.id);
           
            this.svg.append("circle")
                .attr("id", nodeId)
                .attr("cx", stageInputX)
                .attr("cy", stageInputY)
                .style("fill", backgroundColor)
                .style("stroke", borderColor)
                .style("stroke-width", borderWidth)
                .attr("r", stageInputNodeRadius);

            this.svg.append("text")
                .attr("x", stageInputX)
                .attr("y", stageInputY)
                .attr("text-anchor", "middle")
                .attr("dominant-baseline", "central")
                .text(input.componentId)
                .style("fill", fontColor)
                .style("font-family", "Arial, sans-serif")
                .style("font-size", subnodeFontSize);

            // Add edge from stage input to main stage box
            const edgeId = this.elementIdentifier.encodeEdgeId(nodeId, stageNodeId);

            const stageInputConnectingCoordinates = getCirclePoint(stageInputX, stageInputY + stageInputNodeRadius, stageInputNodeRadius, 0.25);

            this.svg.append("line")
                .attr("id", edgeId)
                .attr("x1", stageInputConnectingCoordinates.x) // Connect from the bottom of the circle
                .attr("y1", stageInputConnectingCoordinates.y) 
                .attr("x2", centerX + stageInputRelativeX / 5) // Connect to around the center X and top Y of the box
                .attr("y2", stageBoxY)
                .attr("stroke", subedgeColor)
                .attr("stroke-width", subedgeWidth)
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
        const { backgroundColor, borderColor, borderWidth, fontColor, subnodeFontSize, subedgeColor, subedgeWidth } = GraphUIConfig.node;

        const stageOutputs = nodeUI.node.smallStage.stageOutputs;
        const numberOfOutputs = stageOutputs.length - 1;

        stageOutputs.forEach((output, index) => {
            // Add stage output subnode
            const stageOutputRelativeX =
                numberOfOutputs > 0 ? (index - numberOfOutputs / 2) * (stageWidth / numberOfOutputs) : 0;
            const { x: stageOutputX, y: stageOutputY } = findStageOutputPosition(centerX, centerY, numberOfOutputs, index);

            const nodeId: string = this.elementIdentifier.encodeStageOutputId(stageNodeId, output.id);

            this.svg.append("circle")
                .attr("id", nodeId)
                .attr("cx", stageOutputX)
                .attr("cy", stageOutputY)
                .style("fill", backgroundColor)
                .style("stroke", borderColor)
                .style("stroke-width", borderWidth)
                .attr("r", stageOutputNodeRadius);

            this.svg.append("text")
                .attr("x", stageOutputX)
                .attr("y", stageOutputY)
                .attr("text-anchor", "middle")
                .attr("dominant-baseline", "central")
                .text(output.componentId)
                .style("fill", fontColor)
                .style("font-family", "Arial, sans-serif")
                .style("font-size", subnodeFontSize);

            // Add edge from stage output to main stage box
            const edgeId = this.elementIdentifier.encodeEdgeId(nodeId, stageNodeId);

            const stageOutputConnectingCoordinates = getCirclePoint(stageOutputX, stageOutputY + stageOutputNodeRadius, stageOutputNodeRadius, 0.75);

            this.svg.append("line")
                .attr("id", edgeId)
                .attr("x1", centerX + stageOutputRelativeX / 5) // Connect to around the center X and bottom Y of the box
                .attr("y1", stageBoxY) 
                .attr("x2", stageOutputConnectingCoordinates.x) // Connect from the top of the circle
                .attr("y2", stageOutputConnectingCoordinates.y)
                .attr("stroke", subedgeColor)
                .attr("stroke-width", subedgeWidth)
                .attr("marker-end", "url(#arrowhead)");
        });
    };

    public highlightNode = (node: d3.Selection<d3.BaseType, unknown, HTMLElement, any>) => {
        const { highlightDuration, highlightColor, highlightWidth } = GraphUIConfig.node;
        
        node
            .transition()
            .duration(highlightDuration)
            .style("fill", highlightColor)
            .style("stroke-width", highlightWidth)
            .attr("filter", "url(#drop-shadow)");
    };
    
    public unhighlightAllNodes = () => {
        const { highlightDuration, backgroundColor, borderWidth } = GraphUIConfig.node;
        
        this.svg.selectAll("rect")
            .transition()
            .duration(highlightDuration)
            .style("fill", backgroundColor)
            .style("stroke-width", borderWidth)
            .attr("filter", null);
    };
}