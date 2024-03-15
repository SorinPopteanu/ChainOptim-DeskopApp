declare global {
    interface Window {
        renderGraph: (jsonData: string) => void;
        renderInfo: (infoType: string, isVisible: boolean) => void;
        javaConnector: {
            handleNodeClick: (nodeId: string) => void;
            log: (message: string) => void;
        }
    }
}

export {};