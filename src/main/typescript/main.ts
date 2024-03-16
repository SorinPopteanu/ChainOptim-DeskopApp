import { FactoryProductionGraph, StageNode } from "./types/dataTypes";
import { GraphRenderer } from "./rendering/GraphRenderer";
import { InfoRenderer } from "./rendering/InfoRenderer";
export {};

function renderGraph(jsonData: string) {
    const data: FactoryProductionGraph = JSON.parse(jsonData);

    const graphRenderer = new GraphRenderer("#viz");
    graphRenderer.renderGraph(data);
}

window.renderGraph = renderGraph;