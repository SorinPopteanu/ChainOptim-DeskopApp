package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import com.google.inject.Inject;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class FactoryController implements Initializable {

    private final FactoryService factoryService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;

    private Factory factory;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab productionTab;
    @FXML
    private Tab inventoryTab;
    @FXML
    private Tab performanceTab;

    @FXML
    private Label factoryName;
    @FXML
    private Label factoryLocation;

    @Inject
    public FactoryController(FactoryService factoryService,
                             NavigationService navigationService,
                             CurrentSelectionService currentSelectionService,
                             CommonViewsLoader  commonViewsLoader,
                             FallbackManager fallbackManager) {
        this.factoryService = factoryService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setupListeners();

        Integer factoryId = currentSelectionService.getSelectedId();
        if (factoryId != null) {
            loadFactory(factoryId);
        } else {
            System.out.println("Missing factory id.");
            fallbackManager.setErrorMessage("Failed to load factory.");
        }
    }

    private void loadFactory(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryService.getFactoryById(factoryId)
                .thenApply(this::handleFactoryResponse)
                .exceptionally(this::handleFactoryException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<Factory> handleFactoryResponse(Optional<Factory> factoryOptional) {
        Platform.runLater(() -> {
            if (factoryOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load factory.");
                return;
            }
            this.factory = factoryOptional.get();
            factoryName.setText(factory.getName());
            factoryLocation.setText(factory.getLocation().getFormattedLocation());

            // Load overview tab
            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/factory/FactoryOverviewView.fxml", this.factory);
        });

        return factoryOptional;
    }

    private Optional<Factory> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factory."));
        return Optional.empty();
    }

    private void setupListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/factory/FactoryOverviewView.fxml", this.factory);
            }
        });
        productionTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && productionTab.getContent() == null) {
                commonViewsLoader.loadTabContent(productionTab, "/org/chainoptim/desktop/features/factory/FactoryProductionView.fxml", this.factory);
            }
        });
        inventoryTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && inventoryTab.getContent() == null) {
                commonViewsLoader.loadTabContent(inventoryTab, "/org/chainoptim/desktop/features/factory/FactoryInventoryView.fxml", this.factory);
            }
        });
        performanceTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && performanceTab.getContent() == null) {
                commonViewsLoader.loadTabContent(performanceTab, "/org/chainoptim/desktop/features/factory/FactoryPerformanceView.fxml", this.factory);
            }
        });

        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    @FXML
    private void handleEditFactory() {
        currentSelectionService.setSelectedId(factory.getId());
        navigationService.switchView("Update-Factory?id=" + factory.getId(), true);
    }

}
