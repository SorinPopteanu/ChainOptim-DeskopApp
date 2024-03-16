package org.chainoptim.desktop.features.factory.controller;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.chainoptim.desktop.features.factory.factorygraph.model.*;
import org.chainoptim.desktop.features.factory.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.features.factory.factorygraph.service.JavaConnector;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.apache.commons.text.StringEscapeUtils;
import netscape.javascript.JSObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Float.parseFloat;

public class FactoryProductionController implements DataReceiver<Factory> {

    private final FactoryProductionGraphService graphService;
    private final ResourceAllocationService resourceAllocationService;
    private final FallbackManager fallbackManager;

    private Factory factory;
    private FactoryProductionGraph productionGraph;

    @FXML
    private StackPane graphContainer;
    @FXML
    private WebView webView;

    @FXML
    private CheckBox quantitiesCheckBox;
    @FXML
    private CheckBox capacityCheckBox;
    @FXML
    private CheckBox priorityCheckBox;

    @FXML
    private TextField resourceAllocationInput;

    @FXML
    private ComboBox<String> timePeriodSelect;


    @Inject
    public FactoryProductionController(FactoryProductionGraphService graphService,
                                        ResourceAllocationService resourceAllocationService,
                                        FallbackManager fallbackManager) {
        this.graphService = graphService;
        this.resourceAllocationService = resourceAllocationService;
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
        webView = new WebView();
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
                setupCheckboxListeners();
            }
        });

        graphContainer.getChildren().add(webView);
    }

    private void setupCheckboxListeners() {
        quantitiesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('quantities', " + newValue + ");"));

        capacityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('capacities', " + newValue + ");"));

        priorityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('priorities', " + newValue + ");"));
    }

    @FXML
    private void handleAllocateResources() {
        System.out.println(resourceAllocationInput + " " + timePeriodSelect);
        if (resourceAllocationInput != null && !Objects.equals(resourceAllocationInput.getText(), "") && !Objects.equals(timePeriodSelect.getPromptText(), "")) {
            resourceAllocationService
                    .allocateFactoryResources(factory.getId(), parseFloat(resourceAllocationInput.getText()))
                    .thenApply(this::drawResourceAllocation);
        }
    }

    private AllocationPlan drawResourceAllocation(Optional<AllocationPlan> allocationPlanOptional) {
        if (allocationPlanOptional.isEmpty()) {
            return new AllocationPlan();
        }
        AllocationPlan allocationPlan = allocationPlanOptional.get();
        String jsonString = "{}";
        try {
            jsonString = JsonUtil.getObjectMapper().writeValueAsString(allocationPlan);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        String finalJsonString = jsonString;
        String escapedJsonString = StringEscapeUtils.escapeEcmaScript(finalJsonString);

        String script = "window.renderResourceAllocations('" + escapedJsonString + "');";
        System.out.println("Allocation Plan: " + script);
        // Ensure script execution happens on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return allocationPlan;
    }

}
