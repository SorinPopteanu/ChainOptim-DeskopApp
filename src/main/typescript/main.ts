import { FactoryProductionGraph, GenericGraph, ProductProductionGraph } from "./types/dataTypes";
import { GraphRenderer } from "./rendering/GraphRenderer";
import { transformFactoryToGenericGraph, transformProductToGenericGraph } from "./utils/utils";
export {};

let factoryGraphRenderer: GraphRenderer | null = null;
let productGraphRenderer: GraphRenderer | null = null;

function renderFactoryGraph(jsonData: string) {
    const data: FactoryProductionGraph = JSON.parse(jsonData);

    if (factoryGraphRenderer !== null) {
        factoryGraphRenderer.clearGraph();
    } else {
        factoryGraphRenderer = new GraphRenderer("#viz");
    }

    // Use GenericGraph type to unify rendering logic
    const genericGraph: GenericGraph = transformFactoryToGenericGraph(data.factoryGraph);
    
    factoryGraphRenderer.renderGraph(genericGraph);
}

function renderProductGraph(jsonData: string) {
    const data: ProductProductionGraph = JSON.parse(jsonData);

    if (productGraphRenderer !== null) {
        productGraphRenderer.clearGraph();
    } else {
        productGraphRenderer = new GraphRenderer("#viz");
    }

    // Use GenericGraph type to unify rendering logic
    const genericGraph: GenericGraph = transformProductToGenericGraph(data.productGraph);
    
    productGraphRenderer.renderGraph(genericGraph);
}

window.renderProductGraph = renderProductGraph;
window.renderFactoryGraph = renderFactoryGraph;