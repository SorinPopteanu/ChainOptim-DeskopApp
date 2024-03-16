declare global {
    interface Window {
        renderGraph: (jsonData: string) => void;
        renderInfo: (infoType: string, isVisible: boolean) => void;
        renderResourceAllocations: (jsonData: string) => void;
        javaConnector: {
            handleNodeClick: (nodeId: string) => void;
            log: (message: string) => void;
        }
    }
}

export {};