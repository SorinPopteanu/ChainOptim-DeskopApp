package org.chainoptim.desktop.features.factory.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.features.factory.factorygraph.model.*;
import org.chainoptim.desktop.features.factory.factorygraph.model.SmallStage;
import org.chainoptim.desktop.features.factory.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.productpipeline.model.StageInput;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            displayGraphData();
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

        try {
            String stylesheet = new String(Files.readAllBytes(Paths.get(getClass().getResource("/css/graph.css").toURI())));
            graph.setAttribute("ui.stylesheet", stylesheet);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        int index = 0;

        for (Map.Entry<Integer, StageNode> nodeEntry : productionGraph.getFactoryGraph().getNodes().entrySet()) {
            Integer factoryStageId = nodeEntry.getKey();
            StageNode node = nodeEntry.getValue();
            String nodeId = factoryStageId.toString();

            graph.addNode(nodeId);

            String label = node.getSmallStage().getStageName();
            if (label == null || label.isEmpty()) {
                label = "Stage " + factoryStageId; // Fallback label
            }
            graph.getNode(nodeId).setAttribute("ui.class", "stage");
            graph.getNode(nodeId).setAttribute("ui.label", label);
            float x = index * 30;
            float y = 0;
            graph.getNode(nodeId).setAttribute("xyz", x, y, 0);

            // Draw stage input nodes
            drawGraphStage(node, nodeId, graph, x, y);

            index++;
        }

        // Add edges (only after all nodes populated)
//        for (Map.Entry<Integer, StageNode> nodeEntry : productionGraph.getFactoryGraph().getNodes().entrySet()) {
//            Integer factoryStageId = nodeEntry.getKey();
//            StageNode node = nodeEntry.getValue();
//            for (Edge edge : productionGraph.getFactoryGraph().getAdjList().get(factoryStageId)) {
//                graph.addEdge(factoryStageId.toString() + edge.getOutgoingFactoryStageId(), factoryStageId.toString(), edge.getOutgoingFactoryStageId().toString());
//            }
//
//        }

        FxViewer viewer = new FxViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.disableAutoLayout();

        View view = viewer.addDefaultView(false);

        graphContainer.getChildren().add((Node) view);
    }

    private void drawGraphStage(StageNode node, String stageNodeId, Graph graphUI, float centerX, float centerY) {
        float stageWidth = 20f;
        float stageHeight = 30f;
        float startingX = centerX - stageWidth / 2;
        int index = 0;

        List<SmallStageInput> stageInputs = node.getSmallStage().getStageInputs();
        int numberOfInputs = stageInputs.size() - 1;

        for (SmallStageInput stageInput : stageInputs) {
            float stageInputX = numberOfInputs > 0 ? (startingX + ((float) index / numberOfInputs) * stageWidth) : startingX;
            float stageInputY = centerY + stageHeight / 2;
            index++;

            String nodeId = stageNodeId + ":si:" + stageInput.getId().toString(); // si: to distinguish from stage nodes

            graphUI.addNode(nodeId);
            graphUI.getNode(nodeId).setAttribute("ui.class", "input");
            graphUI.getNode(nodeId).setAttribute("xyz", stageInputX, stageInputY, 0);

            graphUI.addEdge(nodeId + stageNodeId, nodeId, stageNodeId);
        }
    }
}
