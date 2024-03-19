package org.chainoptim.desktop.features.warehouse.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.WarehouseWriteService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateWarehouseController implements Initializable {

    private final WarehouseWriteService warehouseWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    @FXML
    private StackPane fallbackContainer;
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
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.warehouseWriteService = warehouseWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        fallbackManager.setLoading(true);

        Integer organizationId = currentUser.getOrganization().getId();

        CreateWarehouseDTO warehouseDTO = new CreateWarehouseDTO();
        warehouseDTO.setName(nameField.getText());
        warehouseDTO.setOrganizationId(organizationId);
        warehouseDTO.setLocationId(Integer.parseInt(locationIdField.getText()));

        System.out.println(warehouseDTO);

        warehouseWriteService.createWarehouse(warehouseDTO)
                .thenAccept(warehouseOptional -> {
                    Platform.runLater(() -> {
                        if (warehouseOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create new warehouse.");
                            return;
                        }
                        Warehouse warehouse = warehouseOptional.get();
                        fallbackManager.setLoading(false);
                        currentSelectionService.setSelectedId(warehouse.getId());
                        System.out.println(warehouseDTO);
                        navigationService.switchView("Warehouse?id=" + warehouse.getId());
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

}
