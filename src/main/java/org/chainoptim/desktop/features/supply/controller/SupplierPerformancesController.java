package org.chainoptim.desktop.features.supply.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.supply.model.Supplier;
import org.chainoptim.desktop.features.supply.service.SupplierService;
import org.chainoptim.desktop.shared.common.uielements.performance.ScoreDisplay;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.ListHeaderController;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.ListHeaderParams;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class SupplierPerformancesController implements Initializable {

    // Services
    private final SupplierService supplierService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;

    // State
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
    private ScrollPane suppliersScrollPane;
    @FXML
    private VBox suppliersVBox;
    @FXML
    private StackPane headerContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane pageSelectorContainer;

    @Inject
    public SupplierPerformancesController(SupplierService supplierService,
                                          NavigationService navigationService,
                                          CurrentSelectionService currentSelectionService,
                                          CommonViewsLoader commonViewsLoader,
                                          FallbackManager fallbackManager,
                                          SearchParams searchParams) {
        this.supplierService = supplierService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader(new ListHeaderParams(null, searchParams, "Suppliers", "/img/truck-arrow-right-solid.png", Feature.SUPPLIER, sortOptions, null, this::loadSuppliers, "Supplier", "Create-Supplier"));
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadSuppliers();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadSuppliers());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadSuppliers());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadSuppliers());
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadSuppliers());

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
                .exceptionally(this::handleSupplierException);
    }

    private Result<PaginatedResults<Supplier>> handleSupplierResponse(Result<PaginatedResults<Supplier>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
               fallbackManager.setErrorMessage("Failed to load suppliers.");
               return;
            }
            PaginatedResults<Supplier> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalCount);
            int suppliersLimit = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().getMaxSuppliers();
            headerController.disableCreateButton(suppliersLimit != -1 && totalCount >= suppliersLimit, "You have reached the limit of suppliers allowed by your current subscription plan.");

            suppliersVBox.getChildren().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (Supplier supplier : paginatedResults.results) {
                drawSupplierCardUI(supplier);
            }
            fallbackManager.setNoResults(false);
        });
        return result;
    }

    private Result<PaginatedResults<Supplier>> handleSupplierException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load suppliers."));
        return new Result<>();
    }

    private void drawSupplierCardUI(Supplier supplier) {
        HBox cardHBox = new HBox();
        cardHBox.setAlignment(Pos.CENTER);

        // Left side
        VBox supplierBox = new VBox();
        cardHBox.getChildren().add(supplierBox);

        Label supplierName = new Label(supplier.getName());
        supplierName.getStyleClass().add("entity-name-label");
        supplierBox.getChildren().add(supplierName);
        Label supplierLocation = new Label();
        if (supplier.getLocation() != null) {
            supplierLocation.setText(supplier.getLocation().getFormattedLocation());
        } else {
            supplierLocation.setText("");
        }
        supplierLocation.getStyleClass().add("entity-description-label");
        supplierBox.getChildren().add(supplierLocation);

        // Separator
        Region separator = new Region();
        HBox.setHgrow(separator, Priority.ALWAYS);
        cardHBox.getChildren().add(separator);

        // Right side
        HBox supplierScoresHBox = new HBox(8);
        cardHBox.getChildren().add(supplierScoresHBox);
        drawSupplierScores(supplier, supplierScoresHBox);

        Button supplierButton = new Button();
        supplierButton.getStyleClass().add("entity-card");
        supplierButton.setGraphic(cardHBox);
        supplierButton.setMaxWidth(Double.MAX_VALUE);
        supplierButton.prefWidthProperty().bind(suppliersVBox.widthProperty());
        supplierButton.setOnAction(event -> openSupplierDetails(supplier.getId()));

        suppliersVBox.getChildren().add(supplierButton);
    }

    private void drawSupplierScores(Supplier supplier, HBox supplierScoresHBox) {
        addScore(supplier.getOverallScore(), supplierScoresHBox, "Overall Score");
        addScore(supplier.getTimelinessScore(), supplierScoresHBox, "Timeliness Score");
        addScore(supplier.getAvailabilityScore(), supplierScoresHBox, "Availability Score");
        addScore(supplier.getQualityScore(), supplierScoresHBox, "Quality Score");
    }

    private void addScore(Float score, HBox supplierScoresHBox, String tooltipText) {
        ScoreDisplay scoreDisplay = new ScoreDisplay();
        int scoreValue = score != null ? Math.round(score) : 0;
        scoreDisplay.setScore(scoreValue);
        scoreDisplay.setTooltipText(tooltipText);
        supplierScoresHBox.getChildren().add(scoreDisplay);
    }

    private void openSupplierDetails(Integer supplierId) {
        currentSelectionService.setSelectedId(supplierId);
        currentSelectionService.setSelectedPage("Supplier");

        navigationService.switchView("Supplier?id=" + supplierId, true, null);
    }
}

