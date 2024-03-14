package org.chainoptim.desktop.features.factory.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
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
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private Factory factory;

    @FXML
    private FactoryProductionGraphController graphController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane graphContainer;

    @FXML
    private Label factoryName;
    @FXML
    private Label factoryLocation;

    @Inject
    public FactoryController(FactoryService factoryService,
                             CurrentSelectionService currentSelectionService,
                             FXMLLoaderService fxmlLoaderService,
                             ControllerFactory controllerFactory,
                             FallbackManager fallbackManager) {
        this.factoryService = factoryService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
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
        initializeGraph();
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
            System.out.println("Factory: " + factory);
        });

        return factoryOptional;
    }

    private Optional<Factory> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factory."));
        return Optional.empty();
    }


    private void initializeGraph() {
        // Load view into headerContainer and initialize it with appropriate values
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/features/factory/FactoryProductionGraphView.fxml",
                controllerFactory::createController
        );
        try {
            Node graphView = loader.load();
            graphContainer.getChildren().add(graphView);
            adjustGraphLayout();
            graphController = loader.getController();
            graphController.initializeGraph();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void adjustGraphLayout() {
        // Calculate the total height of the header
        double headerHeight = 160;

        // Set the top anchor for graphContainer to be below the header
        AnchorPane.setTopAnchor(graphContainer, headerHeight);
    }

    @FXML
    private void handleEditFactory() {
        System.out.println("Edit Factory Working");
    }

}
