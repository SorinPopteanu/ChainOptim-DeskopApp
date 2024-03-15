import * as d3 from "d3";
import { EdgeUI, StageNodeUI } from "./uiTypes";
import { encodeStageInputId, encodeStageOutputId } from "./utils";
import { calculateEdgePoints } from "./geometryUtils";

export class EdgeRenderer {
    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) {}

        
    renderEdges = (
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
                return;
            }

            const { start, end } = calculateEdgePoints(
                { x: parseFloat(sourceElement.attr("cx")), y: parseFloat(sourceElement.attr("cy")) },
                { x: parseFloat(targetElement.attr("cx")), y: parseFloat(targetElement.attr("cy")) },
                circleRadius,
                circleRadius
            );

            this.svg.append("line")
                .attr("x1", start.x)
                .attr("y1", start.y)
                .attr("x2", end.x)
                .attr("y2", end.y)
                .attr("stroke", "blue")
                .attr("stroke-width", 1)
                .attr("marker-end", "url(#arrowhead)");
        });
    };
}