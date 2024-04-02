package org.chainoptim.desktop.features.product.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

public class ProductsController implements Initializable {

    private final ProductService productService;
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
    private ScrollPane productsScrollPane;
    @FXML
    private VBox productsVBox;
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
    public ProductsController(ProductService productService,
                              NavigationServiceImpl navigationService,
                              CurrentSelectionService currentSelectionService,
                              CommonViewsLoader commonViewsLoader,
                              FallbackManager fallbackManager,
                              SearchParams searchParams
    ) {
        this.productService = productService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader("Products", "/img/box-solid.png", sortOptions, this::loadProducts, "Product", "Create-Product");
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadProducts();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        // Listen to changes in search params
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadProducts());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadProducts());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadProducts());
        searchParams.getPageProperty().addListener((obs, oldPage, newPage) -> loadProducts());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            productsScrollPane.setVisible(newValue);
            productsScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadProducts() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        productService.getProductsByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleProductResponse)
                .exceptionally(this::handleProductException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<PaginatedResults<Product>> handleProductResponse(Optional<PaginatedResults<Product>> productsOptional) {
        Platform.runLater(() -> {
            if (productsOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load products.");
                return;
            }
            productsVBox.getChildren().clear();
            PaginatedResults<Product> paginatedResults = productsOptional.get();
            totalCount = paginatedResults.getTotalCount();

            if (!paginatedResults.results.isEmpty()) {
                for (Product product : paginatedResults.results) {
                    loadProductCardUI(product);
                    Platform.runLater(() -> pageSelectorController.initialize(totalCount));
                }
                fallbackManager.setNoResults(false);
            } else {
                fallbackManager.setNoResults(true);
            }

        });
        return productsOptional;
    }

    private void loadProductCardUI(Product product) {
        Label productName = new Label(product.getName());
        productName.getStyleClass().add("entity-name-label");
        Label productDescription = new Label(product.getDescription());
        productDescription.getStyleClass().add("entity-description-label");
        VBox productBox = new VBox(productName, productDescription);
        Button productButton = new Button();
        productButton.getStyleClass().add("entity-card");
        productButton.setGraphic(productBox);
        productButton.setMaxWidth(Double.MAX_VALUE);
        productButton.prefWidthProperty().bind(productsVBox.widthProperty());
        productButton.setOnAction(event -> openProductDetails(product.getId()));
        productsVBox.getChildren().add(productButton);
    }

    private Optional<PaginatedResults<Product>> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load products."));
        return Optional.empty();
    }

    private void openProductDetails(Integer productId) {
        // Use currentSelectionService to remember the productId
        // And also encode it in the viewKey for caching purposes
        currentSelectionService.setSelectedId(productId);
        currentSelectionService.setSelectedPage("Product");
        navigationService.switchView("Product?id=" + productId, true);
    }
}