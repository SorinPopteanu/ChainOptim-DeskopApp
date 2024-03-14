import { FactoryProductionGraph } from "./types";

import * as d3 from "d3";
export {};

function renderGraph(jsonData: string) {
    const data: FactoryProductionGraph = JSON.parse(jsonData);
    console.log("Data: ", data);
    const width = 400, height = 300;

    // Create SVG container
    const svg = d3.select("#viz").append("svg")
        .attr("width", width)
        .attr("height", height);

    // Draw a simple circle
    svg.append("circle")
        .attr("cx", width / 2)
        .attr("cy", height / 2)
        .attr("r", 50)
        .style("fill", "blue");
}

window.renderGraph = renderGraph;