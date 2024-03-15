import { GraphUIConfig } from "../config/GraphUIConfig";
import { SmallStageInput } from "../types/dataTypes";
import { Coordinates } from "../types/uiTypes";

export const findStageInputPosition = (centerX: number, centerY: number, numberOfInputs: number, index: number): Coordinates => {
    const { stageWidth, stageHeight } = GraphUIConfig.node;
    const stageInputRelativeX =
            numberOfInputs > 0 ? (index - numberOfInputs / 2) * (stageWidth / numberOfInputs) : 0;
    const stageInputX = centerX + stageInputRelativeX;
    const stageInputY = centerY - stageHeight / 2;

    return { x: stageInputX, y: stageInputY };
}

export const findStageOutputPosition = (centerX: number, centerY: number, numberOfOutputs: number, index: number): Coordinates => {
    const { stageWidth, stageHeight } = GraphUIConfig.node;
    const stageOutputRelativeX =
            numberOfOutputs > 0 ? (index - numberOfOutputs / 2) * (stageWidth / numberOfOutputs) : 0;
    const stageOutputX = centerX + stageOutputRelativeX;
    const stageOutputY = centerY + stageHeight / 2;

    return { x: stageOutputX, y: stageOutputY };
}