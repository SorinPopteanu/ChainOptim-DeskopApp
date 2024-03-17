import { AllocationPlan } from "../types/dataTypes";
import { FactoryGraphUI } from "../types/uiTypes";
import { ElementIdentifier } from "../utils/ElementIdentifier";
import { GraphUIConfig } from "../config/GraphUIConfig";

export class ResourceAllocationRenderer {
    private factoryGraphUI: FactoryGraphUI;
    private allocationPlan: AllocationPlan;

    private elementIdentifier: ElementIdentifier;

    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) {
        this.elementIdentifier = new ElementIdentifier();
    }


    public renderResourceAllocations(jsonData: string) {
        const allocationPlan: AllocationPlan = JSON.parse(jsonData);
        this.allocationPlan = allocationPlan;

        Object.entries(this.allocationPlan.factoryGraph.nodes).forEach(([stageNodeId, node]) => {
            let hasDeficits = false;

            let stageInputs = node.smallStage.stageInputs;
            stageInputs.forEach((stageInput, index) => {
                // Find input and inner edge elements
                const inputId = this.elementIdentifier.encodeStageInputId(stageNodeId, stageInput.id);
                const inputElement = this.svg.select(`#${inputId}`);
                const innerEdgeId = this.elementIdentifier.encodeInnerEdgeId(inputId, stageNodeId);
                const innerEdgeElement = this.svg.select(`#${innerEdgeId}`);

                // Determine color based on allocation
                // const correspondingAllocation = this.allocationPlan.allocations.find((allocation) => allocation.stageInputId === stageInput.id);
                // hasDeficits = correspondingAllocation && correspondingAllocation.allocatedAmount < correspondingAllocation.requestedAmount;
                hasDeficits = stageInput.allocatedQuantity < stageInput.requestedQuantity;

                // Apply styling based on deficits
                this.applyDeficitHighlighting(inputElement, hasDeficits);
                this.applyDeficitHighlighting(innerEdgeElement, hasDeficits);

                // Update info texts
                const quantityTextId = this.elementIdentifier.encodeQuantityTextId(stageNodeId, stageInput.id);
                const quantityTextElement = this.svg.select(`#${quantityTextId}`);
                if (!quantityTextElement.empty()) {
                    // Display percentage of requested amount that was allocated
                    const ratio = stageInput.requestedQuantity != 0 ? (stageInput.allocatedQuantity / stageInput.requestedQuantity) : 0;
                    const updatedText = `Q: ${(ratio * 100).toFixed(2)}%`;
                    quantityTextElement.text(updatedText);
                }
            });
            
            node.smallStage.stageOutputs.forEach((stageOutput) => {
                // Find output and inner edge elements
                const outputId = this.elementIdentifier.encodeStageOutputId(stageNodeId, stageOutput.id);
                const outputElement = this.svg.select(`#${outputId}`);
                const innerEdgeId = this.elementIdentifier.encodeInnerEdgeId(stageNodeId, outputId);
                const innerEdgeElement = this.svg.select(`#${innerEdgeId}`);

                // Apply styling based on whether hasDeficits
                this.applyDeficitHighlighting(outputElement, hasDeficits);
                this.applyDeficitHighlighting(innerEdgeElement, hasDeficits);
            });
            
            // Apply styling to the stage node itself
            const encodedStageNodeId = this.elementIdentifier.encodeStageNodeId(stageNodeId)
            const stageNodeElement = this.svg.select(`#${encodedStageNodeId}`);
            this.applyDeficitHighlighting(stageNodeElement, hasDeficits);

            // Update capacity info text
            const capacityTextId = this.elementIdentifier.encodeCapacityTextId(stageNodeId);
            const capacityTextElement = this.svg.select(`#${capacityTextId}`);
            if (!capacityTextElement.empty()) {
                const correspondingStage = Object.values(this.allocationPlan.factoryGraph.nodes).find((node) => node.smallStage.id === parseInt(stageNodeId, 10));
                const updatedText = `C: ${(correspondingStage.allocationCapacityRatio * 100).toFixed(2)}%`;
                capacityTextElement.text(updatedText);
            }
        });

    }

    private applyDeficitHighlighting = (element: d3.Selection<d3.BaseType, unknown, HTMLElement, any>, hasDeficits: boolean) => {
        const { surplusColor, deficitColor, highlightWidth } = GraphUIConfig.resourceAllocation;
        
        const strokeColor = hasDeficits ? deficitColor : surplusColor;
        element.style("stroke", strokeColor).style("stroke-width", highlightWidth);
    };
    

    public setFactoryGraph(factoryGraphUI: FactoryGraphUI) {
        this.factoryGraphUI = factoryGraphUI;
    }
}