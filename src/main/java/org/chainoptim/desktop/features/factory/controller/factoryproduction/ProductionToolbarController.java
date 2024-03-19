package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationService;
import org.chainoptim.desktop.shared.util.TimeUtil;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.util.Objects;
import java.util.Optional;

import static java.lang.Float.parseFloat;
import static org.chainoptim.desktop.shared.util.JsonUtil.prepareJsonString;

public class ProductionToolbarController {

    private final ResourceAllocationService resourceAllocationService;

    private Factory factory;

    private WebView webView;

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
    public ProductionToolbarController(ResourceAllocationService resourceAllocationService) {
        this.resourceAllocationService = resourceAllocationService;
    }

    public void initialize(WebView webView, Factory factory) {
        this.webView = webView;
        this.factory = factory;

        setupCheckboxListeners();
        initializeToolbarUI();
    }

    private void setupCheckboxListeners() {
        quantitiesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('quantities', " + newValue + ");"));

        capacityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('capacities', " + newValue + ");"));

        priorityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('priorities', " + newValue + ");"));
    }

    @FXML
    private void handleAllocateResources() {
        System.out.println("Webview: " + webView);
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
