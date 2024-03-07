package org.chainoptim.desktop.features.product.controller;


import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.controller.HeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.repository.ProductRepository;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class ProductsController implements Initializable {

    private final ProductRepository productRepository;
    private final FallbackManager fallbackManager;
    private final List<String> filtersList = List.of("Created At", "Updated At");

    private final CurrentSelectionService currentSelectionService;
    private final NavigationServiceImpl navigationService;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private VBox productsVBox;
    @FXML
    private StackPane headerContainer;
    private List<Product> products = new ArrayList<>();
    private HeaderController headerController;
    private Integer organizationId = 0;
    private String searchQuery = "";
    private String sortOption = "createdAt";
    private boolean ascending = true;
    private int page = 1;
    private int itemsPerPage = 3;

    @Inject
    public ProductsController(ProductRepository productRepository,
                              FallbackManager fallbackManager,
                              CurrentSelectionService currentSelectionService,
                              NavigationServiceImpl navigationService,
                              HeaderController headerController
    ) {
        this.productRepository = productRepository;
        this.fallbackManager = fallbackManager;
        this.currentSelectionService = currentSelectionService;
        this.navigationService = navigationService;
        this.headerController = headerController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load view into fallbackContainer
        try {
            URL url = getClass().getResource("/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(MainApplication.injector::getInstance);
            Node fallbackView = loader.load();
            fallbackContainer.getChildren().add(fallbackView);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chainoptim/desktop/core/main/HeaderView.fxml"));
            loader.setControllerFactory(MainApplication.injector::getInstance);
            Node headerView = loader.load();
            headerContainer.getChildren().setAll(headerView);
            headerController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception...
        }

        headerController.initializeHeader("Products", "/img/box-solid.png", filtersList, "Product");
        loadProducts();
    }

    private void loadProducts() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }

        fallbackManager.setLoading(true);


        productRepository.getProductsByOrganizationIdAdvanced(organizationId, searchQuery, sortOption, ascending, page, itemsPerPage)
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
            PaginatedResults<Product> paginatedResults = productsOptional.get();

            if (!paginatedResults.results.isEmpty()) {
                for (Product product : paginatedResults.results) {
                    loadProductCardUI(product);
                }
            } else {
                fallbackManager.setNoResults(true);
            }

        });
        return productsOptional;
    }

    private void loadProductCardUI(Product product) {
        Label productName = new Label(product.getName());
        productName.getStyleClass().add("name-label");
        Label productDescription = new Label(product.getDescription());
        productName.getStyleClass().add("description-label");
        VBox productBox = new VBox(productName, productDescription);
        Button productButton = new Button();
        productButton.getStyleClass().add("product-button");
        productButton.setGraphic(productBox);
        productButton.setMaxWidth(Double.MAX_VALUE);
        productButton.prefWidthProperty().bind(productsVBox.widthProperty());
        productButton.setOnAction(event -> openProductDetails(product.getId()));

//        productsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
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

        navigationService.switchView("Product?id=" + productId);
    }


}