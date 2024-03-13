package org.chainoptim.desktop.features.factory.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class FactoryController implements Initializable {

    private final FactoryService factoryService;
    private final CurrentSelectionService currentSelectionService;
    private final FallbackManager fallbackManager;

    private Factory factory;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private Label factoryName;

    @Inject
    public FactoryController(FactoryService factoryService,
                             FallbackManager fallbackManager,
                             CurrentSelectionService currentSelectionService) {
        this.factoryService = factoryService;
        this.fallbackManager = fallbackManager;
        this.currentSelectionService = currentSelectionService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer factoryId = currentSelectionService.getSelectedId();
        if (factoryId == null) {
            System.out.println("Missing factory id.");
            fallbackManager.setErrorMessage("Failed to load factory.");
        }

        loadFactory(factoryId);
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
            System.out.println("Factory: " + factory);
        });

        return factoryOptional;
    }

    private Optional<Factory> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factory."));
        return Optional.empty();
    }


}
