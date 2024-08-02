package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.WarehouseService;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class WarehouseController implements Initializable {
    private final WarehouseService warehouseService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
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
    private Tab storageTab;
    @FXML
    private Label warehouseName;
    @FXML
    private Label warehouseLocation;

    @Inject
    public WarehouseController(WarehouseService warehouseService,
                              NavigationService navigationService,
                              CurrentSelectionService currentSelectionService,
                              CommonViewsLoader commonViewsLoader,
                              FallbackManager fallbackManager) {
        this.warehouseService = warehouseService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setupListeners();

        Integer warehouseId = currentSelectionService.getSelectedId();
        if (warehouseId != null) {
            loadWarehouse(warehouseId);
        } else {
            System.out.println("Missing warehouse id.");
            fallbackManager.setErrorMessage("Failed to load warehouse.");
        }
    }

    private void setupListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/warehouse/WarehouseOverviewView.fxml", this.warehouse);
            }
        });
        inventoryTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && inventoryTab.getContent() == null) {
                commonViewsLoader.loadTabContent(inventoryTab, "/org/chainoptim/desktop/features/warehouse/WarehouseInventoryView.fxml", new SearchData<>(this.warehouse, SearchMode.SECONDARY));
            }
        });
        storageTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && storageTab.getContent() == null) {
                commonViewsLoader.loadTabContent(storageTab, "/org/chainoptim/desktop/features/warehouse/WarehouseStorageView.fxml", this.warehouse);
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

    private Result<Warehouse> handleWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load warehouse.");
                return;
            }
            this.warehouse = result.getData();
            warehouseName.setText(warehouse.getName());
            warehouseLocation.setText(warehouse.getLocation().getFormattedLocation());

            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/warehouse/WarehouseOverviewView.fxml", this.warehouse);
        });
        return result;
    }

    private Result<Warehouse> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouse."));
        return new Result<>();
    }

    @FXML
    private void handleEditWarehouse() {
        currentSelectionService.setSelectedId(warehouse.getId());
        navigationService.switchView("Update-Warehouse?id=" + warehouse.getId(), true, null);
    }
}
