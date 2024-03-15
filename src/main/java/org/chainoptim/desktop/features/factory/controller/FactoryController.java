package org.chainoptim.desktop.features.factory.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class FactoryController implements Initializable {

    private final FactoryService factoryService;
    private final CurrentSelectionService currentSelectionService;
    private final FallbackManager fallbackManager;

    private Factory factory;

    @FXML
    private FactoryProductionController graphController;

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
                             CurrentSelectionService currentSelectionService,
                             FallbackManager fallbackManager) {
        this.factoryService = factoryService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer factoryId = currentSelectionService.getSelectedId();
        if (factoryId == null) {
            System.out.println("Missing factory id.");
            fallbackManager.setErrorMessage("Failed to load factory.");
        }

        loadFactory(factoryId);
        setupTabListeners();
    }

    private void loadFactory(Integer factoryId) {
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
            loadTabContent(overviewTab, "/org/chainoptim/desktop/features/factory/FactoryOverviewView.fxml", this.factory);
        });

        return factoryOptional;
    }

    private Optional<Factory> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factory."));
        return Optional.empty();
    }

    private void setupTabListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                loadTabContent(overviewTab, "/org/chainoptim/desktop/features/factory/FactoryOverviewView.fxml", this.factory);
            }
        });
        productionTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && productionTab.getContent() == null) {
                loadTabContent(productionTab, "/org/chainoptim/desktop/features/factory/FactoryProductionView.fxml", this.factory);
            }
        });
        inventoryTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && inventoryTab.getContent() == null) {
                loadTabContent(inventoryTab, "/org/chainoptim/desktop/features/factory/FactoryInventoryView.fxml", this.factory);
            }
        });
        performanceTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && performanceTab.getContent() == null) {
                loadTabContent(performanceTab, "/org/chainoptim/desktop/features/factory/FactoryPerformanceView.fxml", this.factory);
            }
        });
    }

    private void loadTabContent(Tab tab, String fxmlFilepath, Factory factory) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilepath));
            loader.setControllerFactory(MainApplication.injector::getInstance);
            Node content = loader.load();
            DataReceiver<Factory> controller = loader.getController();
            controller.setData(factory);
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditFactory() {
        System.out.println("Edit Factory Working");
    }

}
