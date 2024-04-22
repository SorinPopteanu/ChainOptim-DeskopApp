package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.WarehouseService;
import org.chainoptim.desktop.features.warehouse.service.WarehouseWriteService;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateWarehouseController implements Initializable {

    private final WarehouseService warehouseService;
    private final WarehouseWriteService warehouseWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
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
            CommonViewsLoader commonViewsLoader
    ) {
        this.warehouseService = warehouseService;
        this.warehouseWriteService = warehouseWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        selectOrCreateLocationController.initialize();
        loadWarehouse(currentSelectionService.getSelectedId());
    }

    private void loadWarehouse(Integer warehouseId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseService.getWarehouseById(warehouseId)
                .thenApply(this::handleWarehouseResponse)
                .exceptionally(this::handleWarehouseException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Warehouse> handleWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load warehouse.");
                return;
            }
            warehouse = result.getData();

            nameField.setText(warehouse.getName());
            selectOrCreateLocationController.setSelectedLocation(warehouse.getLocation());
        });
        return result;
    }

    private Result<Warehouse> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouse."));
        return new Result<>();
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateWarehouseDTO warehouseDTO = getUpdateWarehouseDTO();
        System.out.println(warehouseDTO);

        warehouseWriteService.updateWarehouse(warehouseDTO)
                .thenApply(this::handleUpdateWarehouseResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new Result<>();
                });
    }

    private Result<Warehouse> handleUpdateWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to create warehouse.");
                return;
            }
            fallbackManager.setLoading(false);

            // Manage navigation, invalidating previous warehouse cache
            Warehouse updatedWarehouse = result.getData();
            String warehousePage = "Warehouse?id=" + updatedWarehouse.getId();
            NavigationServiceImpl.invalidateViewCache(warehousePage);
            currentSelectionService.setSelectedId(updatedWarehouse.getId());
            navigationService.switchView(warehousePage, true);
        });
        return result;
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

