package org.chainoptim.desktop.features.test.tudor;


import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProductsTestController implements Initializable {

    private final ProductRepositoryTest productRepository;
    private final FallbackManager fallbackManager;

    private final CurrentSelectionService currentSelectionService;
    private final NavigationService navigationService;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private VBox productsListContainer;

    private List<Product> products = new ArrayList<>();

    @Inject
    public ProductsTestController(ProductRepositoryTest productRepository,
                                  FallbackManager fallbackManager,
                                  CurrentSelectionService currentSelectionService,
                                  NavigationService navigationService
    ) {
        this.productRepository = productRepository;
        this.fallbackManager = fallbackManager;
        this.currentSelectionService = currentSelectionService;
        this.navigationService = navigationService;
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

        loadProducts();
    }

    private void loadProducts() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }

        Integer organizationId = currentUser.getOrganization().getId();
        fallbackManager.setLoading(true);

        productRepository.getProductsByOrganizationId(organizationId)
                .thenApply(this::handleProductResponse)
                .exceptionally(this::handleProductException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<List<Product>> handleProductResponse(Optional<List<Product>> productsOptional) {
        Platform.runLater(() -> {
            if (productsOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load products.");
                return;
            }
            List<Product> products = productsOptional.get();

            if (!products.isEmpty()) {
                this.products = products;
                productsListContainer.getChildren().clear();
                // Display products as buttons routing to ProductDetails
                for (Product product : products) {
                    Button productButton = new Button(product.getName());
                    productButton.setOnAction(event -> openProductDetails(product.getId()));
                    productsListContainer.getChildren().add(productButton);
                }
            } else {
                fallbackManager.setNoResults(true);
            }

        });
        return productsOptional;
    }

    private Optional<List<Product>> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load products."));
        return Optional.empty();
    }

    private void openProductDetails(Integer productId) {
        // Use currentSelectionService to remember the productId
        currentSelectionService.setSelectedProductId(productId);
        navigationService.switchView("Product");
    }


}