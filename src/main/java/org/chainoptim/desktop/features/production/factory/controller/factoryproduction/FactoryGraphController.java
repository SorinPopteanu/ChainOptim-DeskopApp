package org.chainoptim.desktop.features.production.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.production.analysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.features.production.analysis.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

import java.util.List;

import static org.chainoptim.desktop.shared.util.JsonUtil.prepareJsonString;

public class FactoryGraphController {

    private final FactoryProductionGraphService graphService;
    private final FallbackManager fallbackManager;


    private WebView webView;

    @FXML
    private StackPane graphContainer;

    @Inject
    public FactoryGraphController(FactoryProductionGraphService graphService,
                                  FallbackManager fallbackManager) {
        this.graphService = graphService;
        this.fallbackManager = fallbackManager;
    }

    public void initialize(WebView webView) {
        this.webView = webView;
        fallbackManager.setLoading(true);
        loadGraphData();
    }

    private void loadGraphData() {
        graphService.getFactoryGraphById(3)
                .thenApply(this::handleFactoryResponse)
                .exceptionally(this::handleFactoryException);
    }

    private Result<List<FactoryProductionGraph>> handleFactoryResponse(Result<List<FactoryProductionGraph>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null || result.getData().isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load graph.");
                return;
            }
            FactoryProductionGraph productionGraph = result.getData().getFirst();
            fallbackManager.setLoading(false);
            System.out.println("Graph: " + productionGraph);

            displayGraph(productionGraph);
        });
        return result;
    }

    private Result<List<FactoryProductionGraph>> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load graph."));
        return new Result<>();
    }

    private void displayGraph(FactoryProductionGraph productionGraph) {
        if (webView.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            refreshGraph(productionGraph);
        }
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // Execute script for rendering factory graph (using timeout for now to ensure bundle is loaded at this point)
                refreshGraph(productionGraph);
            }
        });

        graphContainer.getChildren().add(webView);
    }

    protected void refreshGraph(FactoryProductionGraph productionGraph) {
        String escapedJsonString = prepareJsonString(productionGraph);

        // Execute script for rendering factory graph (using timeout for now to ensure bundle is loaded at this point)
        String script = "window.renderFactoryGraph('" + escapedJsonString + "');";
        System.out.println("Factory Production Graph Script: " + script);
        try {
            webView.getEngine().executeScript(script);
        } catch (Exception e) {
            fallbackManager.setErrorMessage("Error drawing graph. Please try again.");
            e.printStackTrace();
        }
    }
}
