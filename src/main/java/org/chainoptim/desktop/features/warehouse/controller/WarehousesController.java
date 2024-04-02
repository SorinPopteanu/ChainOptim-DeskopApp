package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.WarehouseService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class WarehousesController implements Initializable {

    private final WarehouseService warehouseService;
    private final NavigationServiceImpl navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;

    @FXML
    private ListHeaderController headerController;
    @FXML
    private PageSelectorController pageSelectorController;
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

    private long totalCount;

    private final Map<String, String> sortOptions = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    @Inject
    public WarehousesController(WarehouseService warehouseService,
                               NavigationServiceImpl navigationService,
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
        headerController.initializeHeader("Warehouses", "/img/warehouse-solid.png", sortOptions, this::loadWarehouses, "Warehouse", "Create-Warehouse");
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
                .exceptionally(this::handleWarehouseException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<PaginatedResults<Warehouse>> handleWarehouseResponse(Optional<PaginatedResults<Warehouse>> warehousesOptional) {
        Platform.runLater(() -> {
            if(warehousesOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load warehouses. ");
                return;
            }
            warehousesVBox.getChildren().clear();
            PaginatedResults<Warehouse> paginatedResults = warehousesOptional.get();
            totalCount = paginatedResults.getTotalCount();

            if(!paginatedResults.results.isEmpty()) {
                for (Warehouse warehouse : paginatedResults.results) {
                    loadWarehouseCardUI(warehouse);
                    Platform.runLater(() -> pageSelectorController.initialize(totalCount));
                }
                fallbackManager.setNoResults(false);
            } else {
                fallbackManager.setNoResults(true);
            }

        });
        return warehousesOptional;
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

    private Optional<PaginatedResults<Warehouse>> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouses."));
        return Optional.empty();
    }

    private void openWarehouseDetails(Integer warehouseId) {
        currentSelectionService.setSelectedId(warehouseId);
        currentSelectionService.setSelectedPage("Warehouse");

        navigationService.switchView("Warehouse?id=" + warehouseId, true);
    }
}
