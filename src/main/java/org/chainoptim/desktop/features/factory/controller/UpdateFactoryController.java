package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryDTO;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryService;
import org.chainoptim.desktop.features.factory.service.FactoryWriteService;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateFactoryController implements Initializable {

    private final FactoryService factoryService;
    private final FactoryWriteService factoryWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;

    private Factory factory;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private TextField nameField;

    @Inject
    public UpdateFactoryController(
            FactoryService factoryService,
            FactoryWriteService factoryWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            CommonViewsLoader commonViewsLoader
    ) {
        this.factoryService = factoryService;
        this.factoryWriteService = factoryWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController =commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        selectOrCreateLocationController.initialize();
        loadFactory(currentSelectionService.getSelectedId());
    }

    private void loadFactory(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryService.getFactoryById(factoryId)
                .thenApply(this::handleFactoryResponse)
                .exceptionally(this::handleFactoryException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Factory> handleFactoryResponse(Result<Factory> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load factory.");
                return;
            }
            factory = result.getData();

            nameField.setText(factory.getName());
            selectOrCreateLocationController.setSelectedLocation(factory.getLocation());
        });
        return result;
    }

    private Result<Factory> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factory."));
        return new Result<>();
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateFactoryDTO factoryDTO = getUpdateFactoryDTO();
        System.out.println(factoryDTO);

        factoryWriteService.updateFactory(factoryDTO)
                .thenApply(this::handleUpdateFactoryResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new Result<>();
                });
    }

    private Result<Factory> handleUpdateFactoryResponse(Result<Factory> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to create factory.");
                return;
            }
            fallbackManager.setLoading(false);

            // Manage navigation, invalidating previous factory cache
            Factory updatedFactory = result.getData();
            String factoryPage = "Factory?id=" + updatedFactory.getId();
            NavigationServiceImpl.invalidateViewCache(factoryPage);
            currentSelectionService.setSelectedId(updatedFactory.getId());
            navigationService.switchView(factoryPage, true);
        });
        return result;
    }

    private UpdateFactoryDTO getUpdateFactoryDTO() {
        UpdateFactoryDTO factoryDTO = new UpdateFactoryDTO();
        factoryDTO.setId(factory.getId());
        factoryDTO.setName(nameField.getText());

        if (selectOrCreateLocationController.isCreatingNewLocation()) {
            factoryDTO.setCreateLocation(true);
            factoryDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
        } else {
            factoryDTO.setCreateLocation(false);
            factoryDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
        }

        return factoryDTO;
    }
}

