import { FactoryProductionGraph, StageNode } from "./types/dataTypes";
import { GraphRenderer } from "./rendering/GraphRenderer";
export {};

let graphRenderer: GraphRenderer | null = null;

function renderGraph(jsonData: string) {
    const data: FactoryProductionGraph = JSON.parse(jsonData);

    if (graphRenderer !== null) {
        graphRenderer.clearGraph();
    } else {
        graphRenderer = new GraphRenderer("#viz");
    }

    graphRenderer.renderGraph(data);
}

window.renderGraph = renderGraph;