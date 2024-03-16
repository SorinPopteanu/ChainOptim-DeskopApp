import { BaseType } from "d3";
import { AllocationPlan, FactoryGraph } from "../types/dataTypes";
import { AllocationPlanUI, FactoryGraphUI, StageNodeUI } from "../types/uiTypes";
import { ElementIdentifier } from "../utils/ElementIdentifier";
import { GraphUIConfig } from "../config/GraphUIConfig";
import { findStageInputPosition } from "../utils/utils";

export class ResourceAllocationRenderer {
    private allocationPlanUI: AllocationPlanUI;
    private factoryGraphUI: FactoryGraphUI;

    private elementIdentifier: ElementIdentifier;

    constructor(private svg: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>) {
        this.elementIdentifier = new ElementIdentifier();
    }


    public renderResourceAllocations(jsonData: string) {
        const allocationPlan: AllocationPlan = JSON.parse(jsonData);
        this.allocationPlanUI = {
            factoryGraph: this.factoryGraphUI,
            inventoryBalance: allocationPlan.inventoryBalance,
            allocationDeficit: allocationPlan.allocationDeficit
        };

        Object.entries(this.allocationPlanUI.factoryGraph.nodes).forEach(([stageNodeId, nodeUI]) => {
            let hasDeficits = false;

            let stageInputs = nodeUI.node.smallStage.stageInputs;
            stageInputs.forEach((stageInput, index) => {
                // Find input and inner edge elements
                const inputId = this.elementIdentifier.encodeStageInputId(stageNodeId, stageInput.id);
                const inputElement = this.svg.select(`#${inputId}`);
                const innerEdgeId = this.elementIdentifier.encodeInnerEdgeId(inputId, stageNodeId);
                const innerEdgeElement = this.svg.select(`#${innerEdgeId}`);

                if (window.javaConnector) {
                    window.javaConnector.log("Attempting to select inner edge with  ID: " + innerEdgeId);
                }
                // Determine color based on allocation
                const correspondingDeficit = this.allocationPlanUI.allocationDeficit.find((deficit) => deficit.stageInputId === stageInput.id);
                hasDeficits = correspondingDeficit && correspondingDeficit.allocatedAmount < correspondingDeficit.requestedAmount;

                // Apply styling based on deficits
                this.applyDeficitHighlighting(inputElement, hasDeficits);
                this.applyDeficitHighlighting(innerEdgeElement, hasDeficits);

                const { x: stageInputX, y: stageInputY } = findStageInputPosition(nodeUI.coordinates.x, nodeUI.coordinates.x, stageInputs.length - 1, index);
                
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