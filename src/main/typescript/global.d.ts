declare global {
    interface Window {
        renderGraph: (jsonData: string) => void;
        javaConnector: {
            handleNodeClick: (nodeId: string) => void;
            log: (message: string) => void;
        }
    }
}

export {};