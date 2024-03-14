export function encodeStageInputId(stageId: number, inputId: number) {
    return `s_${stageId}_si_${inputId}`;
}

export function encodeStageOutputId(stageId: number, outputId: number) {
    return `s_${stageId}_so_${outputId}`;
}