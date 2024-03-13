package org.chainoptim.desktop.features.factory.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

public class FactoryProductionGraphController {

    @FXML
    private StackPane graphContainer;

    public void initializeGraph() {
        Graph graph = new SingleGraph("FactoryGraph");
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        graph.addNode("A").setAttribute("ui.label", "A");
        graph.addNode("B").setAttribute("ui.label", "B");
        graph.addNode("C").setAttribute("ui.label", "C");

        graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");

        graph.setAttribute("ui.stylesheet",
                "node { fill-color: red; size: 20px; text-alignment: above; }" +
                        "edge { fill-color: grey; size: 1px; }");

        graph.getNode("A").setAttribute("xyz", 0, 1, 0);
        graph.getNode("B").setAttribute("xyz", 1, 0, 0);
        graph.getNode("C").setAttribute("xyz", 2, 1, 0);

        FxViewer viewer = new FxViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.disableAutoLayout();

        View view = viewer.addDefaultView(false);

        Platform.runLater(() -> {
            graphContainer.getChildren().add((Node) view);
        });
    }
}
