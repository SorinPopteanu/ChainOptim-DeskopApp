export class ElementIdentifier {
    constructor() {}

    encodeStageNodeId(stageNodeId: number | string) {
        return `s_${stageNodeId}`;
    }

    encodeStageInputId(stageId: number | string, inputId: number | string) {
        return `s_${stageId}_si_${inputId}`;
    }
    
    encodeStageOutputId(stageId: number | string, outputId: number | string) {
        return `s_${stageId}_so_${outputId}`;
    }

    encodeInnerEdgeId(nodeId1: number | string, nodeId2: number | string) {
        return `ie_${nodeId1}_${nodeId2}`;
    }

}