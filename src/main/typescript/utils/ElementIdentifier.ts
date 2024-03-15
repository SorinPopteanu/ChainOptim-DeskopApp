export class ElementIdentifier {
    constructor() {}

    encodeStageInputId(stageId: number, inputId: number) {
        return `s_${stageId}_si_${inputId}`;
    }
    
    encodeStageOutputId(stageId: number, outputId: number) {
        return `s_${stageId}_so_${outputId}`;
    }

    encodeEdgeId(nodeId: string, stageNodeId: number) {
        return `e_${nodeId}_${stageNodeId}`;
    }

}