package org.chainoptim.desktop.features.supplier.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.service.SupplierService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class SuppliersController implements Initializable {

    private final SupplierService supplierService;
    private final NavigationServiceImpl navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;

    @FXML
    private ListHeaderController headerController;
    @FXML
    private PageSelectorController pageSelectorController;
    @FXML
    private ScrollPane suppliersScrollPane;
    @FXML
    private VBox suppliersVBox;
    @FXML
    private StackPane headerContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane pageSelectorContainer;

    private long totalCount;

    private final Map<String, String> sortOptions = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    @Inject
    public SuppliersController(SupplierService supplierService,
                               NavigationServiceImpl navigationService,
                               CurrentSelectionService currentSelectionService,
                               FXMLLoaderService fxmlLoaderService,
                               ControllerFactory controllerFactory,
                               FallbackManager fallbackManager,
                               SearchParams searchParams
   ) {
        this.supplierService = supplierService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeHeader();
        loadFallbackManager();
        setUpListeners();
        loadSuppliers();
        initializePageSelector();
    }

    private void initializeHeader() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/main/ListHeaderView.fxml",
                controllerFactory::createController
        );
        try {
            Node headerView = loader.load();
            headerContainer.getChildren().add(headerView);
            headerController = loader.getController();
            headerController.initializeHeader("Suppliers", "/img/truck-arrow-right-solid.png", sortOptions, "Supplier", "Create-Supplier");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void initializePageSelector() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/search/PageSelectorView.fxml",
                controllerFactory::createController
        );
        try {
            Node pageSelectorView = loader.load();
            pageSelectorContainer.getChildren().add(pageSelectorView);
            pageSelectorController = loader.getController();
            searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadSuppliers());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpListeners() {
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadSuppliers());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadSuppliers());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadSuppliers());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            suppliersScrollPane.setVisible(newValue);
            suppliersScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadSuppliers() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        supplierService.getSuppliersByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleSupplierResponse)
                .exceptionally(this::handleSupplierException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<PaginatedResults<Supplier>> handleSupplierResponse(Optional<PaginatedResults<Supplier>> suppliersOptional) {
        Platform.runLater(() -> {
           if (suppliersOptional.isEmpty()) {
               fallbackManager.setErrorMessage("Failed to load suppliers.");
               return;
           }
           suppliersVBox.getChildren().clear();
           PaginatedResults<Supplier> paginatedResults = suppliersOptional.get();
           totalCount = paginatedResults.getTotalCount();

           if (!paginatedResults.results.isEmpty()) {
               for (Supplier supplier : paginatedResults.results) {
                   loadSupplierCardUI(supplier);
                   Platform.runLater(() -> pageSelectorController.initialize(totalCount));
               }
               fallbackManager.setNoResults(false);
           } else {
               fallbackManager.setNoResults(true);
           }
        });
        return suppliersOptional;
    }

    private void loadSupplierCardUI(Supplier supplier) {
        Label supplierName = new Label(supplier.getName());
        supplierName.getStyleClass().add("entity-name-label");
        Label supplierLocation = new Label();
        if (supplier.getLocation() != null) {
            supplierLocation.setText(supplier.getLocation().getFormattedLocation());
        } else {
            supplierLocation.setText("");
        }
        supplierLocation.getStyleClass().add("entity-description-label");

        VBox supplierBox = new VBox(supplierName, supplierLocation);
        Button supplierButton = new Button();
        supplierButton.getStyleClass().add("entity-card");
        supplierButton.setGraphic(supplierBox);
        supplierButton.setMaxWidth(Double.MAX_VALUE);
        supplierButton.prefWidthProperty().bind(suppliersVBox.widthProperty());
        supplierButton.setOnAction(event -> openSupplierDetails(supplier.getId()));

        suppliersVBox.getChildren().add(supplierButton);
    }

    private Optional<PaginatedResults<Supplier>> handleSupplierException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load suppliers."));
        return Optional.empty();
    }

    private void openSupplierDetails(Integer supplierId) {
        currentSelectionService.setSelectedId(supplierId);
        currentSelectionService.setSelectedPage("Supplier");

        navigationService.switchView("Supplier?id=" + supplierId);
    }







}

