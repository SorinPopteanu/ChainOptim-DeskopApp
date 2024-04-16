package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.core.context.TenantSettingsContext;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.model.ProductionToolbarActionListener;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationService;
import org.chainoptim.desktop.shared.common.uielements.info.InfoLabel;
import org.chainoptim.desktop.shared.common.uielements.select.SelectDurationController;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.InfoLevel;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import lombok.Setter;
import com.google.inject.Inject;

import java.util.Objects;
import java.util.Optional;

import static org.chainoptim.desktop.shared.util.JsonUtil.prepareJsonString;

public class FactoryProductionToolbarController {

    // Services
    private final ResourceAllocationService resourceAllocationService;
    private final CommonViewsLoader commonViewsLoader;

    // Listeners
    @Setter
    private ProductionToolbarActionListener actionListener;

    // State
    private Factory factory;
    private AllocationPlan allocationPlan;

    // FXML
    private WebView webView;

    // - Edit Configuration
    @FXML
    private Button toggleEditConfigurationButton;
    @FXML
    private VBox editConfigurationContentVBox;
    @FXML
    private Button addStageButton;
    @FXML
    private Button updateStageButton;
    @FXML
    private Button deleteStageButton;
    @FXML
    private Button addConnectionButton;
    @FXML
    private Button deleteConnectionButton;

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
    private VBox computePlanVBox;
    @FXML
    private Button viewActivePlan;
    @FXML
    private StackPane durationInputContainer;
    private SelectDurationController selectDurationController;
    @FXML
    private Button viewAllocationPlanButton;
    @FXML
    private Button viewProductionHistoryButton;

    // - Seek Resources
    @FXML
    private Button toggleSeekResourcesButton;
    @FXML
    private VBox seekResourcesContentBox;

    // Info Labels
    @FXML
    private InfoLabel stageInfoLabel;
    @FXML
    private InfoLabel resourceAllocationInfoLabel;
    @FXML
    private InfoLabel productionHistoryInfoLabel;

    // - Icons
    private Image addImage;
    private Image updateImage;
    private Image deleteImage;
    private Image angleUpImage;
    private Image angleDownImage;
    private Image eyeImage;

    @Inject
    public FactoryProductionToolbarController(ResourceAllocationService resourceAllocationService,
                                              CommonViewsLoader commonViewsLoader) {
        this.resourceAllocationService = resourceAllocationService;
        this.commonViewsLoader = commonViewsLoader;
    }

    public void initialize(WebView webView, Factory factory) {
        this.webView = webView;
        this.factory = factory;

        initializeToolbarUI();
        setupCheckboxListeners();
    }

    // Initialization
    private void initializeToolbarUI() {
        initializeIcons();
        initializeButtons();
        initializeInfoLabels();

        selectDurationController = commonViewsLoader.loadSelectDurationView(durationInputContainer);
    }

    private void initializeIcons() {
        angleUpImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-up-solid.png")));
        angleDownImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-down-solid.png")));
        addImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
        updateImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pen-to-square-solid.png")));
        deleteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
        eyeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/eye-solid.png")));
    }

    private void initializeButtons() {
        toggleEditConfigurationButton.setGraphic(createImageView(angleUpImage));
        toggleDisplayInfoButton.setGraphic(createImageView(angleUpImage));
        viewActivePlan.setGraphic(createImageView(eyeImage));
        viewAllocationPlanButton.setGraphic(createImageView(eyeImage));
        toggleResourceAllocationButton.setGraphic(createImageView(angleUpImage));
        toggleSeekResourcesButton.setGraphic(createImageView(angleUpImage));
        addStageButton.setGraphic(createImageView(addImage));
        updateStageButton.setGraphic(createImageView(updateImage));
        deleteStageButton.setGraphic(createImageView(deleteImage));
        addConnectionButton.setGraphic(createImageView(addImage));
        deleteConnectionButton.setGraphic(createImageView(deleteImage));

        viewAllocationPlanButton.setVisible(false);
        viewAllocationPlanButton.setManaged(false);
    }

    private void initializeInfoLabels() {
        InfoLevel currentLevel = TenantSettingsContext.getCurrentUserSettings().getGeneralSettings().getInfoLevel();
        stageInfoLabel.setFeatureAndLevel(Feature.FACTORY_STAGE, currentLevel);
        resourceAllocationInfoLabel.setFeatureAndLevel(Feature.RESOURCE_ALLOCATION_PLAN, currentLevel);
        productionHistoryInfoLabel.setFeatureAndLevel(Feature.FACTORY_PRODUCTION_HISTORY, currentLevel);
    }

    private void setupCheckboxListeners() {
        quantitiesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('quantities', " + newValue + ");"));

        capacityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('capacities', " + newValue + ");"));

        priorityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> webView.getEngine().executeScript("window.renderInfo('priorities', " + newValue + ");"));
    }

    // Actions
    // - Allocate resources
    @FXML
    private void handleAllocateResources() {
        Float durationSeconds = selectDurationController.getTimeSeconds();

        resourceAllocationService
                .allocateFactoryResources(factory.getId(), durationSeconds)
                .thenApply(this::handleAllocationPlanResponse);
    }

    private AllocationPlan handleAllocationPlanResponse(Optional<AllocationPlan> allocationPlanOptional) {
        if (allocationPlanOptional.isEmpty()) {
            return new AllocationPlan();
        }
        allocationPlan = allocationPlanOptional.get();
        updateViewAllocationPlanButtonVisibility();

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

    @FXML
    private void openAddStageAction() {
        actionListener.onOpenAddStageRequested();
    }

    @FXML
    private void openUpdateStageAction() {
        actionListener.onOpenUpdateStageRequested();
    }

    @FXML
    private void deleteStageAction() {
//        actionListener.onDeleteStageRequested();
    }

    @FXML
    private void addConnectionAction() {
    }

    @FXML
    private void deleteConnectionAction() {
    }

    @FXML
    private void openAllocationPlan() {
        actionListener.onOpenAllocationPlanRequested(allocationPlan, false);
    }

    @FXML
    private void openCurrentAllocationPlan() {
        actionListener.onOpenAllocationPlanRequested(allocationPlan, true);
    }

    @FXML
    private void openProductionHistory() {
        actionListener.onOpenProductionHistoryRequested();
    }

    @FXML
    private void toggleComputePlanSubsection() {
        boolean isVisible = computePlanVBox.isVisible();
        computePlanVBox.setVisible(!isVisible);
        computePlanVBox.setManaged(!isVisible);
    }

    // - Toggle Toolbar sections
    @FXML
    private void toggleEditConfigurationSection() {
        toggleSection(editConfigurationContentVBox, toggleEditConfigurationButton);
    }

    @FXML
    private void toggleDisplayInfoSection() {
        toggleSection(displayInfoContentVBox, toggleDisplayInfoButton);
    }

    @FXML
    private void toggleResourceAllocationSection() {
        toggleSection(resourceAllocationContentBox, toggleResourceAllocationButton);
    }

    @FXML
    private void toggleSeekResourcesSection() {
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

    // Utils
    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void updateViewAllocationPlanButtonVisibility() {
        // Update button visibility based on whether an allocation plan is available
        System.out.println("Allocation plan: " + allocationPlan);
        boolean isAllocationPlanAvailable = allocationPlan != null;
        viewAllocationPlanButton.setVisible(isAllocationPlanAvailable);
        viewAllocationPlanButton.setManaged(isAllocationPlanAvailable);
    }
}
