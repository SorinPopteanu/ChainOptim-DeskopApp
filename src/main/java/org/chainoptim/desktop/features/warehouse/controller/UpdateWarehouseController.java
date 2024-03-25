package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.WarehouseService;
import org.chainoptim.desktop.features.warehouse.service.WarehouseWriteService;
import org.chainoptim.desktop.shared.common.uielements.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateWarehouseController implements Initializable {

    private final WarehouseService warehouseService;
    private final WarehouseWriteService warehouseWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private Warehouse warehouse;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private TextField nameField;

    @Inject
    public UpdateWarehouseController(
            WarehouseService warehouseService,
            WarehouseWriteService warehouseWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.warehouseService = warehouseService;
        this.warehouseWriteService = warehouseWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        loadSelectOrCreateLocation();
        loadWarehouse(currentSelectionService.getSelectedId());
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadSelectOrCreateLocation() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectOrCreateLocationView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectOrCreateLocationView = loader.load();
            selectOrCreateLocationController = loader.getController();
            selectOrCreateLocationContainer.getChildren().add(selectOrCreateLocationView);
            selectOrCreateLocationController.initialize();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadWarehouse(Integer warehouseId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseService.getWarehouseById(warehouseId)
                .thenApply(this::handleWarehouseResponse)
                .exceptionally(this::handleWarehouseException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<Warehouse> handleWarehouseResponse(Optional<Warehouse> warehouseOptional) {
        Platform.runLater(() -> {
            if (warehouseOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load warehouse.");
                return;
            }
            warehouse = warehouseOptional.get();

            nameField.setText(warehouse.getName());
            selectOrCreateLocationController.setSelectedLocation(warehouse.getLocation());
        });

        return warehouseOptional;
    }

    private Optional<Warehouse> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouse."));
        return Optional.empty();
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateWarehouseDTO warehouseDTO = getUpdateWarehouseDTO();
        System.out.println(warehouseDTO);

        warehouseWriteService.updateWarehouse(warehouseDTO)
                .thenAccept(warehouseOptional ->
                    Platform.runLater(() -> {
                        if (warehouseOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create warehouse.");
                            return;
                        }
                        fallbackManager.setLoading(false);

                        // Manage navigation, invalidating previous warehouse cache
                        Warehouse updatedWarehouse = warehouseOptional.get();
                        String warehousePage = "Warehouse?id=" + updatedWarehouse.getId();
                        NavigationServiceImpl.invalidateViewCache(warehousePage);
                        currentSelectionService.setSelectedId(updatedWarehouse.getId());
                        navigationService.switchView(warehousePage, true);
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private UpdateWarehouseDTO getUpdateWarehouseDTO() {
        UpdateWarehouseDTO warehouseDTO = new UpdateWarehouseDTO();
        warehouseDTO.setId(warehouse.getId());
        warehouseDTO.setName(nameField.getText());

        if (selectOrCreateLocationController.isCreatingNewLocation()) {
            warehouseDTO.setCreateLocation(true);
            warehouseDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
        } else {
            warehouseDTO.setCreateLocation(false);
            warehouseDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
        }

        return warehouseDTO;
    }
}

