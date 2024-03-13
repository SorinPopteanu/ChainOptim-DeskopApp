package org.chainoptim.desktop.features.factory.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.features.factory.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.features.factory.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FactoryProductionGraphController {

    private final FactoryProductionGraphService graphService;

    private final FallbackManager fallbackManager;

    @FXML
    private StackPane graphContainer;

    private FactoryProductionGraph productionGraph;

    @Inject
    public FactoryProductionGraphController(
                                        FactoryProductionGraphService graphService,
                                        FallbackManager fallbackManager
    ) {
        this.graphService = graphService;
        this.fallbackManager = fallbackManager;
    }

    public void initializeGraph() {
        loadGraphData();
        displayGraphData();
    }

    private void loadGraphData() {
        graphService.getFactoryGraphById(3).thenApply(this::handleFactoryResponse)
                .exceptionally(this::handleFactoryException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private List<FactoryProductionGraph> handleFactoryResponse(List<FactoryProductionGraph> productionGraphs) {
        Platform.runLater(() -> {
            if (productionGraphs.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load graph.");
                return;
            }
            this.productionGraph = productionGraphs.getFirst();
            System.out.println("Graph: " + productionGraph);
        });

        return productionGraphs;
    }

    private List<FactoryProductionGraph> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load graph."));
        return new ArrayList<>();
    }

    private void displayGraphData() {
        // Get data into graph
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

        Platform.runLater(() -> graphContainer.getChildren().add((Node) view));
    }
}
