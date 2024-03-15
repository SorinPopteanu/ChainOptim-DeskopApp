package org.chainoptim.desktop.features.factory.controller;

import javafx.scene.control.CheckBox;
import org.chainoptim.desktop.features.factory.factorygraph.model.*;
import org.chainoptim.desktop.features.factory.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.features.factory.factorygraph.service.JavaConnector;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.apache.commons.text.StringEscapeUtils;
import netscape.javascript.JSObject;

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
import java.util.Objects;

public class FactoryProductionController implements DataReceiver<Factory> {

    private final FactoryProductionGraphService graphService;
    private final FallbackManager fallbackManager;

    private Factory factory;
    private FactoryProductionGraph productionGraph;

    @FXML
    private StackPane graphContainer;

    @FXML
    private CheckBox quantitiesCheckBox;
    @FXML
    private CheckBox capacityCheckBox;
    @FXML
    private CheckBox priorityCheckBox;

    @Inject
    public FactoryProductionController(FactoryProductionGraphService graphService,
                                        FallbackManager fallbackManager) {
        this.graphService = graphService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
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
            displayGraph();
        });

        return productionGraphs;
    }

    private List<FactoryProductionGraph> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load graph."));
        return new ArrayList<>();
    }

    private void displayGraph() {
        WebView webView = new WebView();
        webView.getEngine().load(Objects.requireNonNull(getClass().getResource("/html/graph.html")).toExternalForm());

        String jsonString = "{}";
        try {
            jsonString = JsonUtil.getObjectMapper().writeValueAsString(productionGraph);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        String finalJsonString = jsonString;
        String escapedJsonString = StringEscapeUtils.escapeEcmaScript(finalJsonString);

        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {

                // Execute script for rendering factory graph (using timeout for now to ensure bundle is loaded at this point)
                String script = "setTimeout(function() { renderGraph('" + escapedJsonString + "'); }, 200);";
                try {
                    webView.getEngine().executeScript(script);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Set up connector between JavaFX and Typescript
                JSObject jsObject = (JSObject) webView.getEngine().executeScript("window");
                jsObject.setMember("javaConnector", new JavaConnector());

                // Set up listeners
                setupCheckboxListeners(webView);
            }
        });

        graphContainer.getChildren().add(webView);
    }

    private void setupCheckboxListeners(WebView webView) {
        quantitiesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            webView.getEngine().executeScript("window.renderInfo('quantities', " + newValue + ");");
        });

        capacityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            webView.getEngine().executeScript("window.renderInfo('capacities', " + newValue + ");");
        });

        priorityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            webView.getEngine().executeScript("window.renderInfo('priorities', " + newValue + ");");
        });
    }

}
