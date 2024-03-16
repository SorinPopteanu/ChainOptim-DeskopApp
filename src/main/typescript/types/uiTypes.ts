import { Edge, FactoryInventoryItem, ResourceAllocation, StageNode } from "./dataTypes";

export interface FactoryGraphUI {
    nodes: Record<number, StageNodeUI>;
    adjList: Record<number, EdgeUI[]>;
    pipelinePriority: number;
}

export interface StageNodeUI {
    node: StageNode;
    coordinates?: Coordinates; // Center of the node's stage box
    visited?: boolean;
}

export interface EdgeUI {
    edge: Edge;

}


// Resource allocation
export interface AllocationPlanUI {
    factoryGraph: FactoryGraphUI;
    inventoryBalance: Record<number, FactoryInventoryItem>;
    allocationDeficit: ResourceAllocation[];
}








// Utils
export interface Coordinates {
    x: number;
    y: number;
}
