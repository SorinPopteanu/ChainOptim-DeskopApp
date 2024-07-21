package org.chainoptim.desktop.features.supplier.controller;

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
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import org.chainoptim.desktop.features.supplier.service.SupplierService;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {

    private final SupplierService supplierService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private Supplier supplier;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab ordersTab;
    @FXML
    private Tab shipmentsTab;
    @FXML
    private Tab performanceTab;
    @FXML
    private Label supplierName;
    @FXML
    private Label supplierLocation;

    @Inject
    public SupplierController(SupplierService supplierService,
                              NavigationService navigationService,
                              CurrentSelectionService currentSelectionService,
                              ControllerFactory controllerFactory,
                              FallbackManager fallbackManager) {
        this.supplierService = supplierService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListeners();

        Integer supplierId = currentSelectionService.getSelectedId();
        if (supplierId != null) {
            loadSupplier(supplierId);
        } else {
            System.out.println("Missing supplier id.");
            fallbackManager.setErrorMessage("Failed to load supplier.");
        }
    }

    private void setupListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                loadTabContent(overviewTab, "/org/chainoptim/desktop/features/supplier/SupplierOverviewView.fxml", this.supplier);
            }
        });
        ordersTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && ordersTab.getContent() == null) {
                loadTabContent(ordersTab, "/org/chainoptim/desktop/features/supplier/SupplierOrdersView.fxml", this.supplier);
            }
        });
        shipmentsTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && shipmentsTab.getContent() == null) {
                loadTabContent(shipmentsTab, "/org/chainoptim/desktop/features/supplier/SupplierShipmentsView.fxml", this.supplier);
            }
        });
        performanceTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && performanceTab.getContent() == null) {
                loadTabContent(performanceTab, "/org/chainoptim/desktop/features/supplier/SupplierPerformanceView.fxml", this.supplier);
            }
        });
    }

    private void loadSupplier(Integer supplierId) {
        fallbackManager.setLoading(true);

        supplierService.getSupplierById(supplierId)
                .thenApply(this::handleSupplierResponse)
                .exceptionally(this::handleSupplierException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Supplier> handleSupplierResponse(Result<Supplier> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load supplier.");
                return;
            }
            this.supplier = result.getData();
            supplierName.setText(supplier.getName());

            if (supplier.getLocation() != null) {
                supplierLocation.setText(supplier.getLocation().getFormattedLocation());
            } else {
                supplierLocation.setText("");
            }

            loadTabContent(overviewTab, "/org/chainoptim/desktop/features/supplier/SupplierOverviewView.fxml", this.supplier);
        });
        return result;
    }

    private Result<Supplier> handleSupplierException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier."));
        return new Result<>();
    }

    private void loadTabContent(Tab tab, String fxmlFilepath, Supplier supplier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilepath));
            loader.setControllerFactory(controllerFactory::createController);
            Node content = loader.load();
            DataReceiver<SearchData<Supplier>> controller = loader.getController();
            controller.setData(new SearchData<>(supplier, SearchMode.SECONDARY));
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditSupplier() {
        currentSelectionService.setSelectedId(supplier.getId());
        navigationService.switchView("Update-Supplier?id=" + supplier.getId(), true, null);
    }
}
