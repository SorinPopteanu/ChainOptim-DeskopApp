import { AllocationPlan, FactoryGraph } from "../types/dataTypes";
import { AllocationPlanUI, FactoryGraphUI } from "../types/uiTypes";
import { ElementIdentifier } from "../utils/ElementIdentifier";

export class ResourceAllocationRenderer {
    private allocationPlanUI: AllocationPlanUI;
    private factoryGraphUI: FactoryGraphUI;

    private elementIdentifier: ElementIdentifier;

    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) {
        this.elementIdentifier = new ElementIdentifier();
    }


    public renderResourceAllocations(jsonData: string) {
        const allocationPlan: AllocationPlan = JSON.parse(jsonData);
        if (window.javaConnector) {
            window.javaConnector.log("Rendering resource allocation: " + allocationPlan);
        }
        this.allocationPlanUI = {
            factoryGraph: this.mergeGraphAndGraphUI(allocationPlan.factoryGraph, this.factoryGraphUI),
            inventoryBalance: allocationPlan.inventoryBalance,
            allocationDeficit: allocationPlan.allocationDeficit
        };

        Object.entries(this.allocationPlanUI.factoryGraph.nodes).forEach(([nodeId, nodeUI]) => {
            nodeUI.node.smallStage.stageInputs.forEach((stageInput) => {
                const inputId = this.elementIdentifier.encodeStageInputId(nodeId, stageInput.id);
                const inputElement = this.svg.select(`#${inputId}`);
                inputElement
                    .style("fill", "green");
            });
            nodeUI.node.smallStage.stageOutputs.forEach((stageOutput) => {
                const outputId = this.elementIdentifier.encodeStageOutputId(nodeId, stageOutput.id);
                const outputElement = this.svg.select(`#${outputId}`);
                outputElement
                    .style("fill", "green");
            });
        });

    }


    private mergeGraphAndGraphUI(factoryGraphWithAllocations: FactoryGraph, factoryGraphUI: FactoryGraphUI): FactoryGraphUI {
        // TODO: Replace in the future
        const mergedGraph: FactoryGraphUI = factoryGraphUI;
        Object.entries(factoryGraphWithAllocations.nodes).forEach(([nodeId, nodeUI]) => {
            mergedGraph.nodes[parseInt(nodeId, 10)].node.smallStage.stageInputs.forEach((stageInput) => {
                nodeUI.smallStage.stageInputs.find((stageInputUI) => stageInputUI.id === stageInput.id).allocatedQuantity = stageInput.allocatedQuantity;
            });
            mergedGraph.nodes[parseInt(nodeId, 10)].node.smallStage.stageOutputs.forEach((stageOutput) => {
                nodeUI.smallStage.stageOutputs.find((stageOutputUI) => stageOutputUI.id === stageOutput.id).expectedOutputPerAllocation = stageOutput.expectedOutputPerAllocation;
            });
        });
        return mergedGraph;
    }

    public setFactoryGraph(factoryGraphUI: FactoryGraphUI) {
        this.factoryGraphUI = factoryGraphUI;
    }
}