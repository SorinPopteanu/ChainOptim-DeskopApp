export class ElementIdentifier {
    constructor() {}

    // Nodes
    encodeStageNodeId(stageNodeId: number | string) {
        return `s_${stageNodeId}`;
    }

    encodeStageInputId(stageId: number | string, inputId: number | string) {
        return `s_${stageId}_si_${inputId}`;
    }
    
    encodeStageOutputId(stageId: number | string, outputId: number | string) {
        return `s_${stageId}_so_${outputId}`;
    }

    // Edges
    encodeInnerEdgeId(nodeId1: number | string, nodeId2: number | string) {
        return `ie_${nodeId1}_${nodeId2}`;
    }

    // Info texts
    encodeQuantityTextId(nodeId: number | string, inputId: number | string) {
        return `quantity-text-${nodeId}-${inputId}`;
    }

    encodeCapacityTextId(nodeId: number | string) {
        return `capacity-text-${nodeId}`;
    }

    encodePriorityTextId(nodeId: number | string) {
        return `priority-text-${nodeId}`;
    }


}