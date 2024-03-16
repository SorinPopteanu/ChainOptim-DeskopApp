package org.chainoptim.desktop.features.factory.factorygraph.service;

public class JavaConnector {

    public void renderInfo(String infoType, Boolean isVisible) {
        System.out.println("Info type: " + infoType + " is visible: " + isVisible);
    }

    public void handleNodeClick(String nodeId) {
        System.out.println("Node clicked: " + nodeId);
    }

    public void log(String message) {
        System.out.println(message);
    }
}
