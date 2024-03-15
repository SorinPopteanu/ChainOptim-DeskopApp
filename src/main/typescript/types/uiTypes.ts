import { Edge, StageNode } from "./dataTypes";

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












export interface Coordinates {
    x: number;
    y: number;
}
