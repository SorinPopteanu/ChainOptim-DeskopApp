package org.chainoptim.desktop.features.production.analysis.factorygraph.service;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class JavaConnector {

    private Integer selectedNodeId;

    // Production Graphs
    public void renderInfo(String infoType, Boolean isVisible) {
        System.out.println("Info type: " + infoType + " is visible: " + isVisible);
    }

    public void handleNodeClick(String nodeId) {
        System.out.println("Node clicked: " + nodeId);
        Pattern pattern = Pattern.compile("s_(\\d+)");
        Matcher matcher = pattern.matcher(nodeId);

        if (matcher.matches()) {
            selectedNodeId = Integer.parseInt(matcher.group(1));
            System.out.println("Extracted Node ID: " + selectedNodeId);
        } else {
            System.out.println("The node ID format is incorrect: " + nodeId);
        }
    }

    // Supply Chain Map
    public void handleMapNodeClick(String nodeId) {
        System.out.println("Map Node clicked: " + nodeId);
    }

    // Utils
    public void log(String message) {
        System.out.println(message);
    }
}
