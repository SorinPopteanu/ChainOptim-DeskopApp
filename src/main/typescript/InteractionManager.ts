import * as d3 from "d3";
import { NodeRenderer } from "./NodeRenderer";

export class InteractionManager {
    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>,
                private nodeRenderer: NodeRenderer) {}

    setupNodeInteractions(handleNodeClick: (nodeId: string) => void) {
        this.svg.selectAll('rect').on("click", (event) => {
            const clickedNodeId = d3.select(event.currentTarget).attr("id");
            this.nodeRenderer.unhighlightAllNodes(); // Unhighlight all nodes first
            this.nodeRenderer.highlightNode(clickedNodeId); // Then highlight the clicked node
            handleNodeClick(clickedNodeId); // Finally, execute any additional click handling logic
        });
    }

}