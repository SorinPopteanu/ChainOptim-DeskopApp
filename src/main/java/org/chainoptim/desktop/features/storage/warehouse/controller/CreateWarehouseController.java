package org.chainoptim.desktop.features.storage.warehouse.controller;

import org.chainoptim.desktop.core.main.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.features.storage.warehouse.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.storage.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.storage.warehouse.service.WarehouseWriteService;
import org.chainoptim.desktop.shared.common.ui.forms.FormField;
import org.chainoptim.desktop.shared.common.ui.forms.ValidationException;
import org.chainoptim.desktop.shared.common.ui.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.common.ui.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.common.ui.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateWarehouseController implements Initializable {

    // Services
    private final WarehouseWriteService warehouseWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    // Controllers
    private SelectOrCreateLocationController selectOrCreateLocationController;

    // FXML
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private FormField<String> nameFormField;

    @Inject
    public CreateWarehouseController(
            WarehouseWriteService warehouseWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            ToastManager toastManager,
            FallbackManager fallbackManager,
            CommonViewsLoader commonViewsLoader) {
        this.warehouseWriteService = warehouseWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);

        initializeFormFields();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, null, "Your input is not valid.");
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateWarehouseDTO warehouseDTO = getCreateWarehouseDTO(organizationId);
        if (warehouseDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseWriteService.createWarehouse(warehouseDTO)
                .thenApply(this::handleCreateWarehouseResponse)
                .exceptionally(this::handleCreateWarehouseException);
    }

    private CreateWarehouseDTO getCreateWarehouseDTO(Integer organizationId) {
        CreateWarehouseDTO warehouseDTO = new CreateWarehouseDTO();
        try {
            warehouseDTO.setName(nameFormField.handleSubmit());
            warehouseDTO.setOrganizationId(organizationId);
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

    private Result<Warehouse> handleCreateWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to create warehouse.", OperationOutcome.ERROR));
                return;
            }
            Warehouse warehouse = result.getData();
            toastManager.addToast(new ToastInfo(
                    "Success", "Warehouse created successfully.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(warehouse.getId());
            navigationService.switchView("Warehouse?id=" + warehouse.getId(), true, null);
        });
        return result;
    }

    private Result<Warehouse> handleCreateWarehouseException(Throwable ex) {
        Platform.runLater(() ->
            toastManager.addToast(new ToastInfo(
                    "Error", "Failed to create warehouse.", OperationOutcome.ERROR)));
        return new Result<>();
    }

}

