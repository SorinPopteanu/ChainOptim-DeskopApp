import { BaseType } from "d3";
import { AllocationPlan, FactoryGraph } from "../types/dataTypes";
import { FactoryGraphUI, StageNodeUI } from "../types/uiTypes";
import { ElementIdentifier } from "../utils/ElementIdentifier";
import { GraphUIConfig } from "../config/GraphUIConfig";
import { findStageInputPosition } from "../utils/utils";

export class ResourceAllocationRenderer {
    private factoryGraphUI: FactoryGraphUI;
    private allocationPlan: AllocationPlan;

    private elementIdentifier: ElementIdentifier;

    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) {
        this.elementIdentifier = new ElementIdentifier();
    }


    public renderResourceAllocations(jsonData: string) {
        const allocationPlan: AllocationPlan = JSON.parse(jsonData);
        this.allocationPlan = {
            inventoryBalance: allocationPlan.inventoryBalance,
            allocations: allocationPlan.allocations
        };

        Object.entries(this.factoryGraphUI.nodes).forEach(([stageNodeId, nodeUI]) => {
            let hasDeficits = false;

            let stageInputs = nodeUI.node.smallStage.stageInputs;
            stageInputs.forEach((stageInput, index) => {
                // Find input and inner edge elements
                const inputId = this.elementIdentifier.encodeStageInputId(stageNodeId, stageInput.id);
                const inputElement = this.svg.select(`#${inputId}`);
                const innerEdgeId = this.elementIdentifier.encodeInnerEdgeId(inputId, stageNodeId);
                const innerEdgeElement = this.svg.select(`#${innerEdgeId}`);

                // Determine color based on allocation
                const correspondingAllocation = this.allocationPlan.allocations.find((allocation) => allocation.stageInputId === stageInput.id);
                hasDeficits = correspondingAllocation && correspondingAllocation.allocatedAmount < correspondingAllocation.requestedAmount;

                // Apply styling based on deficits
                this.applyDeficitHighlighting(inputElement, hasDeficits);
                this.applyDeficitHighlighting(innerEdgeElement, hasDeficits);

                // Update info texts
                const quantityTextId = this.elementIdentifier.encodeQuantityTextId(stageNodeId, stageInput.id);
                const quantityTextElement = this.svg.select(`#${quantityTextId}`);
                if (!quantityTextElement.empty() && correspondingAllocation) {
                    const updatedText = `Q: ${correspondingAllocation.allocatedAmount.toFixed(0)}/${correspondingAllocation.requestedAmount.toFixed(0)}`;
                    quantityTextElement.text(updatedText);
                }


            });
            nodeUI.node.smallStage.stageOutputs.forEach((stageOutput) => {
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