declare global {
    interface Window {
        renderGraph: (jsonData: string) => void;
    }
}

export {};