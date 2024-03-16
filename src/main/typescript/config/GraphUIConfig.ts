export const GraphUIConfig = {
    graph: {
        width: 800,
        height: 600,
        spaceBetweenStagesX: 240,
        spaceBetweenStagesY: 200,
        paddingX: 100,
        paddingY: 100,
    },
    node: {
        stageWidth: 100,
        stageHeight: 140,
        stageBoxWidth: 90,
        stageBoxHeight: 60,
        subnodeRadius: 14,
        backgroundColor: "#f0f0f0",
        borderColor: "gray",
        borderWidth: 1,
        borderRadius: 4,
        fontColor: "black",
        mainNodeFontSize: 14,
        subnodeFontSize: 10,
        subedgeColor: "black",
        subedgeWidth: 1,
        highlightDuration: 200,
        highlightColor: "#d9e2ef",
        highlightWidth: 2,
    },
    edge: {
        color: "blue",
        width: 1,
        markerEnd: "url(#arrowhead)",
    },
    info: {
        infoFontSize: 14,
        infoColor: "black",
        infoPaddingX: 20,
        capacityPaddingY: -10,
        priorityPaddingY: 10
    },
    resourceAllocation: {
        surplusColor: "green",
        deficitColor: "red",
        highlightWidth: 1.5,
        allocationRatioPaddingX: -20,
        allocationRatioPaddingY: 0,
    },
    shadow: {
        id: "drop-shadow",
        dx: 1,
        dy: 2,
        stdDeviation: 3,
    }
}