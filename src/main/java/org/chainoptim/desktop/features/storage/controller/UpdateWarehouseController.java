package org.chainoptim.desktop.features.storage.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.storage.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.storage.model.Warehouse;
import org.chainoptim.desktop.features.storage.service.WarehouseService;
import org.chainoptim.desktop.features.storage.service.WarehouseWriteService;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateWarehouseController implements Initializable {

    // Services
    private final WarehouseService warehouseService;
    private final WarehouseWriteService warehouseWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    // Controllers
    private SelectOrCreateLocationController selectOrCreateLocationController;

    // State
    private Warehouse warehouse;

    // FXML
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private FormField<String> nameFormField;

    @Inject
    public UpdateWarehouseController(
            WarehouseService warehouseService,
            WarehouseWriteService warehouseWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager) {
        this.warehouseService = warehouseService;
        this.warehouseWriteService = warehouseWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        loadWarehouse(currentSelectionService.getSelectedId());
    }

    private void loadWarehouse(Integer warehouseId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseService.getWarehouseById(warehouseId)
                .thenApply(this::handleWarehouseResponse)
                .exceptionally(this::handleWarehouseException);
    }

    private Result<Warehouse> handleWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load warehouse.");
                return;
            }
            warehouse = result.getData();
            fallbackManager.setLoading(false);

            initializeFormFields();
            selectOrCreateLocationController.setSelectedLocation(warehouse.getLocation());
        });

        return result;
    }

    private Result<Warehouse> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouse."));
        return new Result<>();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, warehouse.getName(), "Your input is not valid");
    }

    @FXML
    private void handleSubmit() {
        UpdateWarehouseDTO warehouseDTO = getUpdateWarehouseDTO();
        if (warehouseDTO == null) return;
        System.out.println(warehouseDTO);

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseWriteService.updateWarehouse(warehouseDTO)
                .thenApply(this::handleUpdateWarehouseResponse)
                .exceptionally(this::handleUpdateWarehouseException);
    }

    private UpdateWarehouseDTO getUpdateWarehouseDTO() {
        UpdateWarehouseDTO warehouseDTO = new UpdateWarehouseDTO();
        warehouseDTO.setId(warehouse.getId());
        try {
            warehouseDTO.setName(nameFormField.handleSubmit());

            if (selectOrCreateLocationController.isCreatingNewLocation()) {
                warehouseDTO.setCreateLocation(true);
                warehouseDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
            } else {
                warehouseDTO.setCreateLocation(false);
                warehouseDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
            }
        } catch (ValidationException e) {
            return null;
        }

        return warehouseDTO;
    }

    private Result<Warehouse> handleUpdateWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to update warehouse.", OperationOutcome.ERROR));
                return;
            }
            toastManager.addToast(new ToastInfo(
                    "Success", "Warehouse updated successfully.", OperationOutcome.SUCCESS));

            // Manage navigation, invalidating previous warehouse cache
            Warehouse updatedWarehouse = result.getData();
            String warehousePage = "Warehouse?id=" + updatedWarehouse.getId();
            NavigationServiceImpl.invalidateViewCache(warehousePage);
            currentSelectionService.setSelectedId(updatedWarehouse.getId());
            navigationService.switchView(warehousePage, true, null);
        });
        return result;
    }

    private Result<Warehouse> handleUpdateWarehouseException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to update warehouse.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}

