package org.chainoptim.desktop.features.warehouse.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.service.SupplierService;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.WarehouseService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class WarehouseController implements Initializable {
    private final WarehouseService warehouseService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private Warehouse warehouse;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab inventoryTab;
    @FXML
    private Label warehouseName;
    @FXML
    private Label warehouseLocation;

    @Inject
    public WarehouseController(WarehouseService warehouseService,
                              CurrentSelectionService currentSelectionService,
                              FXMLLoaderService fxmlLoaderService,
                              ControllerFactory controllerFactory,
                              FallbackManager fallbackManager) {
        this.warehouseService = warehouseService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize (URL location, ResourceBundle resources) {
        loadFallbackManager();
        setupListeners();
        Integer warehouseId = currentSelectionService.getSelectedId();
        if (warehouseId != null) {
            loadWarehouse(warehouseId);
        } else {
            System.out.println("Missing warehouse id.");
            fallbackManager.setErrorMessage("Failed to load warehouse.");
        }
    }

    private void loadFallbackManager() {
        // Load view into fallbackContainer
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void setupListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                loadTabContent(overviewTab, "/org/chainoptim/desktop/features/warehouse/WarehouseOverviewView.fxml", this.warehouse);
            }
        });
        inventoryTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && inventoryTab.getContent() == null) {
                loadTabContent(inventoryTab, "/org/chainoptim/desktop/features/warehouse/WarehouseInventoryView.fxml", this.warehouse);
            }
        });

        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
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
            this.warehouse = warehouseOptional.get();
            warehouseName.setText(warehouse.getName());
            warehouseLocation.setText(warehouse.getLocation().getFormattedLocation());

            loadTabContent(overviewTab, "/org/chainoptim/desktop/features/warehouse/WarehouseOverviewView.fxml", this.warehouse);
        });

        return warehouseOptional;
    }

    private Optional<Warehouse> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouse."));
        return Optional.empty();
    }

    private void loadTabContent(Tab tab, String fxmlFilepath, Warehouse warehouse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilepath));
            loader.setControllerFactory(MainApplication.injector::getInstance);
            Node content = loader.load();
            DataReceiver<Warehouse> controller = loader.getController();
            controller.setData(warehouse);
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditWarehouse() {
        System.out.println("Edit Warehouse Working");
    }

}
