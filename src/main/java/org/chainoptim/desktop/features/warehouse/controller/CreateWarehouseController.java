package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
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
import java.util.ResourceBundle;

public class CreateWarehouseController implements Initializable {

    private final WarehouseWriteService warehouseWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private TextField nameField;
    @FXML
    private TextField locationIdField;

    @Inject
    public CreateWarehouseController(
            WarehouseWriteService warehouseWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            CommonViewsLoader commonViewsLoader
    ) {
        this.warehouseWriteService = warehouseWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        selectOrCreateLocationController.initialize();
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateWarehouseDTO warehouseDTO = getCreateWarehouseDTO(organizationId);
        System.out.println(warehouseDTO);

        warehouseWriteService.createWarehouse(warehouseDTO)
                .thenApply(this::handleCreateWarehouseResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new Result<>();
                });
    }

    private Result<Warehouse> handleCreateWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to create new warehouse.");
                return;
            }
            Warehouse warehouse = result.getData();
            fallbackManager.setLoading(false);
            currentSelectionService.setSelectedId(warehouse.getId());
            navigationService.switchView("Warehouse?id=" + warehouse.getId(), true);
        });
        return result;
    }

    private CreateWarehouseDTO getCreateWarehouseDTO(Integer organizationId) {
        CreateWarehouseDTO warehouseDTO = new CreateWarehouseDTO();
        warehouseDTO.setName(nameField.getText());
        warehouseDTO.setOrganizationId(organizationId);
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
