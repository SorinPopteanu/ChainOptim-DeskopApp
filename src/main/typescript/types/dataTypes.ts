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

// Components and products

export interface Component {
    id: number;
    name: string;
    description: string;
    createdAt: string;
    updatedAt: string;
    organizationId: number;
    unitId: number;
}

export interface Product {
    id: number;
    name: string;
    description: string;
    createdAt: string;
    updatedAt: string;
    organizationId: number;
    unitId: number;
}


// Inventory
export interface FactoryInventoryItem {
    id: number;
    factoryId: number;
    component: Component;
    product: Product;
    createdAt: string;
    updatedAt: string;
    quantity: number;
    minimumRequiredQuantity: number;
}

// Resource Allocation
export interface AllocationPlan {
    factoryGraph: FactoryGraph;
    inventoryBalance: Record<number, FactoryInventoryItem>;
    allocationDeficit: ResourceAllocation[];
}

export interface ResourceAllocation {
    stageInputId: number;
    componentId: number;
    allocatorInventoryItemId: number;
    allocatedAmount: number;
    requestedAmount: number;
}