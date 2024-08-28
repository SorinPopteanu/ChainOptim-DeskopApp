package org.chainoptim.desktop.features.goods.product.controller.productproduction;

import org.chainoptim.desktop.features.goods.productgraph.model.ProductProductionGraph;
import org.chainoptim.desktop.features.goods.productgraph.service.ProductProductionGraphService;
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
        graphService.getProductGraphById(21)
                .thenApply(this::handleProductResponse)
                .exceptionally(this::handleProductException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<List<ProductProductionGraph>> handleProductResponse(Result<List<ProductProductionGraph>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load graph.");
                return;
            }
            ProductProductionGraph productionGraph = result.getData().getFirst();
            displayGraph(productionGraph);
        });
        return result;
    }

    private Result<List<ProductProductionGraph>> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load graph."));
        return new Result<>();
    }

    private void displayGraph(ProductProductionGraph productionGraph) {
        if (webView.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            refreshGraph(productionGraph);
        }
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                refreshGraph(productionGraph);
            }
        });

        graphContainer.getChildren().add(webView);
    }

    protected void refreshGraph(ProductProductionGraph productionGraph) {
        String escapedJsonString = prepareJsonString(productionGraph);

        // Execute script for rendering factory graph (using timeout for now to ensure bundle is loaded at this point)
        String script = "window.renderProductGraph('" + escapedJsonString + "');";
        System.out.println("Product Production Graph Script: " + script);
        try {
            webView.getEngine().executeScript(script);
        } catch (Exception e) {
            fallbackManager.setErrorMessage("Error drawing graph. Please try again.");
            e.printStackTrace();
        }
    }
}
