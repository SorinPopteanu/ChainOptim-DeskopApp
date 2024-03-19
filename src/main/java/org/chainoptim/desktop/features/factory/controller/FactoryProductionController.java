package org.chainoptim.desktop.features.factory.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.JavaConnector;
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
import org.chainoptim.desktop.shared.util.TimeUtil;

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

    // Graph
    @FXML
    private StackPane graphContainer;
    @FXML
    private WebView webView;

    // Toolbar elements
    // - Edit Configuration
    @FXML
    private Button toggleEditConfigurationButton;
    @FXML
    private VBox editConfigurationContentVBox;

    // - Display Info
    @FXML
    private Button toggleDisplayInfoButton;
    @FXML
    private VBox displayInfoContentVBox;
    @FXML
    private CheckBox quantitiesCheckBox;
    @FXML
    private CheckBox capacityCheckBox;
    @FXML
    private CheckBox priorityCheckBox;

    // - Resource Allocation
    @FXML
    private Button toggleResourceAllocationButton;
    @FXML
    private VBox resourceAllocationContentBox;
    @FXML
    private TextField resourceAllocationInput;
    @FXML
    private ComboBox<String> timePeriodSelect;

    // - Seek Resources
    @FXML
    private Button toggleSeekResourcesButton;
    @FXML
    private VBox seekResourcesContentBox;

    // - Icons
    private Image angleUpImage;
    private Image angleDownImage;

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
        initializeToolbarUI();
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

        String escapedJsonString = prepareJsonString(productionGraph);

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
        if (resourceAllocationInput != null && !Objects.equals(resourceAllocationInput.getText(), "") && timePeriodSelect.getValue() != null) {
            float inputDuration = parseFloat(resourceAllocationInput.getText());
            float durationSeconds = TimeUtil.getSeconds(inputDuration, timePeriodSelect.getValue());
            if (durationSeconds == -1.0f) return;

            resourceAllocationService
                    .allocateFactoryResources(factory.getId(), durationSeconds)
                    .thenApply(this::drawResourceAllocation);
        }
    }

    private AllocationPlan drawResourceAllocation(Optional<AllocationPlan> allocationPlanOptional) {
        if (allocationPlanOptional.isEmpty()) {
            return new AllocationPlan();
        }
        AllocationPlan allocationPlan = allocationPlanOptional.get();
        String escapedJsonString = prepareJsonString(allocationPlan);

        String script = "window.renderResourceAllocations('" + escapedJsonString + "');";

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

    private <T> String prepareJsonString(T data) {
        String jsonString = "{}";
        try {
            jsonString = JsonUtil.getObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        String finalJsonString = jsonString;
        return StringEscapeUtils.escapeEcmaScript(finalJsonString);
    }

    // Toolbar
    private void initializeToolbarUI() {
        angleUpImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-up-solid.png")));
        angleDownImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-down-solid.png")));

        toggleEditConfigurationButton.setGraphic(createImageView(angleUpImage));
        toggleDisplayInfoButton.setGraphic(createImageView(angleUpImage));
        toggleResourceAllocationButton.setGraphic(createImageView(angleUpImage));
        toggleSeekResourcesButton.setGraphic(createImageView(angleUpImage));
    }

    // Toggle Toolbar sections
    @FXML
    private void toggleEditConfigurationSection(ActionEvent event) {
        toggleSection(editConfigurationContentVBox, toggleEditConfigurationButton);
    }

    @FXML
    private void toggleDisplayInfoSection(ActionEvent event) {
        toggleSection(displayInfoContentVBox, toggleDisplayInfoButton);
    }

    @FXML
    private void toggleResourceAllocationSection(ActionEvent event) {
        toggleSection(resourceAllocationContentBox, toggleResourceAllocationButton);
    }

    @FXML
    private void toggleSeekResourcesSection(ActionEvent event) {
        toggleSection(seekResourcesContentBox, toggleSeekResourcesButton);
    }

    private void toggleSection(VBox sectionVBox, Button sectionToggleButton) {
        boolean isVisible = sectionVBox.isVisible();
        sectionVBox.setVisible(!isVisible);
        sectionVBox.setManaged(!isVisible);
        if (isVisible) {
            sectionToggleButton.setGraphic(createImageView(angleDownImage));
        } else {
            sectionToggleButton.setGraphic(createImageView(angleUpImage));
        }
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }
}
