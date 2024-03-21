import { FactoryEdge, FactoryInventoryItem, ResourceAllocation, FactoryStageNode } from "./dataTypes";

export interface FactoryGraphUI {
    nodes: Record<number, StageNodeUI>;
    adjList: Record<number, EdgeUI[]>;
    pipelinePriority: number;
}

export interface StageNodeUI {
    node: FactoryStageNode;
    coordinates?: Coordinates; // Center of the node's stage box
    visited?: boolean;
}

export interface EdgeUI {
    edge: FactoryEdge;

}





// Utils
export interface Coordinates {
    x: number;
    y: number;
}
