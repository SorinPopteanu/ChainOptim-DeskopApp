package org.chainoptim.desktop.features.storage.warehouse.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.shared.search.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.storage.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.storage.warehouse.service.WarehouseService;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.ListHeaderParams;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class WarehousesController implements Initializable {

    // Services
    private final WarehouseService warehouseService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;

    // Settings
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;
    private long totalCount;

    private final Map<String, String> sortOptions = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    // Controllers
    private ListHeaderController headerController;
    private PageSelectorController pageSelectorController;

    // FXML
    @FXML
    private ScrollPane warehousesScrollPane;
    @FXML
    private VBox warehousesVBox;
    @FXML
    private StackPane headerContainer;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private StackPane fallbackContainer;


    @Inject
    public WarehousesController(WarehouseService warehouseService,
                               NavigationService navigationService,
                               CurrentSelectionService currentSelectionService,
                               CommonViewsLoader commonViewsLoader,
                               FallbackManager fallbackManager,
                               SearchParams searchParams
    ) {
        this.warehouseService = warehouseService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader(new ListHeaderParams(null, searchParams, "Warehouses", "/img/warehouse-solid.png", Feature.WAREHOUSE, sortOptions, null, this::loadWarehouses, "Warehouse", "Create-Warehouse"));
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadWarehouses();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadWarehouses());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadWarehouses());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadWarehouses());
        searchParams.getPageProperty().addListener((obs, oldPage, newPage) -> loadWarehouses());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            warehousesScrollPane.setVisible(newValue);
            warehousesScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadWarehouses() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        warehouseService.getWarehousesByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleWarehouseResponse)
                .exceptionally(this::handleWarehouseException);
    }

    private Result<PaginatedResults<Warehouse>> handleWarehouseResponse(Result<PaginatedResults<Warehouse>> result) {
        Platform.runLater(() -> {
            if(result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load warehouses. ");
                return;
            }
            PaginatedResults<Warehouse> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalCount);
            int warehousesLimit = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().getMaxWarehouses();
            headerController.disableCreateButton(warehousesLimit != -1 && totalCount >= warehousesLimit, "You have reached the limit of warehouses allowed by your current subscription plan.");

            warehousesVBox.getChildren().clear();
            if(paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (Warehouse warehouse : paginatedResults.results) {
                loadWarehouseCardUI(warehouse);
            }
            fallbackManager.setNoResults(false);
        });
        return result;
    }

    private Result<PaginatedResults<Warehouse>> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouses."));
        return new Result<>();
    }

    private void loadWarehouseCardUI(Warehouse warehouse) {
        Label warehouseName = new Label(warehouse.getName());
        warehouseName.getStyleClass().add("entity-name-label");
        Label warehouseLocation = new Label();
        if (warehouse.getLocation() != null) {
            warehouseLocation.setText(warehouse.getLocation().getFormattedLocation());
        } else {
            warehouseLocation.setText("");
        }
        warehouseLocation.getStyleClass().add("entity-description-label");

        VBox warehouseBox = new VBox(warehouseName, warehouseLocation);
        Button warehouseButton = new Button();
        warehouseButton.getStyleClass().add("entity-card");
        warehouseButton.setGraphic(warehouseBox);
        warehouseButton.setMaxWidth(Double.MAX_VALUE);
        warehouseButton.prefWidthProperty().bind(warehousesVBox.widthProperty());
        warehouseButton.setOnAction(event -> openWarehouseDetails(warehouse.getId()));

        warehousesVBox.getChildren().add(warehouseButton);
    }

    private void openWarehouseDetails(Integer warehouseId) {
        currentSelectionService.setSelectedId(warehouseId);
        currentSelectionService.setSelectedPage("Warehouse");

        navigationService.switchView("Warehouse?id=" + warehouseId, true, null);
    }
}
