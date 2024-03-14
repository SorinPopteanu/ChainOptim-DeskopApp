export interface FactoryProductionGraph {
    id: number;
    factoryId: number;
    createdAt: string;
    updatedAt: string;
    factoryGraph: FactoryGraph;
}

export interface FactoryGraph {
    nodes: Record<number, StageNode>;
    adjList: Record<number, Edge[]>;
    pipelinePriority: number;
}

export interface StageNode {
    smallStage: SmallStage;
    numberOfStepsCapacity: number;
    perDuration: number;
    priority: number;
}

export interface SmallStage {
    id: number;
    stageName: string;
    stageInputs: SmallStageInput[];
    stageOutputs: SmallStageOutput[];
}

export interface SmallStageInput {
    id: number;
    componentId: number;
    quantityPerStage: number;
    allocatedQuantity: number;
}

export interface SmallStageOutput {
    id: number;
    componentId: number;
    quantityPerStage: number;
    expectedOutputPerAllocation: number;
}

export interface Edge {
    incomingFactoryStageId: number;
    incomingStageOutputId: number;
    outgoingFactoryStageId: number;
    outgoingStageInputId: number;
}