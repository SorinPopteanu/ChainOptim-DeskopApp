package org.chainoptim.desktop.features.product.controller.productproduction;

import org.chainoptim.desktop.features.scanalysis.productgraph.model.ProductProductionGraph;
import org.chainoptim.desktop.features.scanalysis.productgraph.service.ProductProductionGraphService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.List;

import static org.chainoptim.desktop.shared.util.JsonUtil.prepareJsonString;

public class ProductGraphController {

    private final ProductProductionGraphService graphService;
    private final FallbackManager fallbackManager;

    private WebView webView;

    @FXML
    private StackPane graphContainer;

    @Inject
    public ProductGraphController(ProductProductionGraphService graphService,
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
        graphService.getProductGraphById(3).thenApply(this::handleProductResponse)
                .exceptionally(this::handleProductException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private List<ProductProductionGraph> handleProductResponse(List<ProductProductionGraph> productionGraphs) {
        Platform.runLater(() -> {
            if (productionGraphs.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load graph.");
                return;
            }
            ProductProductionGraph productionGraph = productionGraphs.getFirst();
            System.out.println("Graph: " + productionGraph);
            displayGraph(productionGraph);
        });

        return productionGraphs;
    }

    private List<ProductProductionGraph> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load graph."));
        return new ArrayList<>();
    }

    private void displayGraph(ProductProductionGraph productionGraph) {
        String escapedJsonString = prepareJsonString(productionGraph);

        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {

                // Execute script for rendering factory graph (using timeout for now to ensure bundle is loaded at this point)
                String script = "setTimeout(function() { renderGraph('" + escapedJsonString + "'); }, 200);";
                System.out.println("Production Graph Script: " + script);
                try {
                    webView.getEngine().executeScript(script);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        graphContainer.getChildren().add(webView);
    }

    protected void refreshGraph(ProductProductionGraph productionGraph) {
        String escapedJsonString = prepareJsonString(productionGraph);

        // Execute script for rendering factory graph (using timeout for now to ensure bundle is loaded at this point)
        String script = "window.renderGraph('" + escapedJsonString + "');";
        System.out.println("Production Graph Script: " + script);
        try {
            webView.getEngine().executeScript(script);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
