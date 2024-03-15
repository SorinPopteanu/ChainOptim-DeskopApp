import * as d3 from "d3";
import { NodeRenderer } from "./NodeRenderer";

export class InteractionManager {
    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
                private nodeRenderer: NodeRenderer) {}

    setupNodeInteractions(handleNodeClick: (nodeId: string) => void) {
        this.svg.selectAll('rect').on("click", (event) => {
            const clickedNode = d3.select(event.currentTarget);
            const clickedNodeId = clickedNode.attr("id");
            // Unhighlight all nodes and highlight the clicked one
            this.nodeRenderer.unhighlightAllNodes();
            this.nodeRenderer.highlightNode(clickedNode);
            // Execute any additional logic
            handleNodeClick(clickedNodeId);
        });
    }

}
