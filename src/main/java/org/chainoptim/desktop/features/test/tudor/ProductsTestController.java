package org.chainoptim.desktop.features.test.tudor;


import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.context.TenantContext;
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

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private ListView<String> productsListView;

    private List<Product> products = new ArrayList<>();

    @Inject
    public ProductsTestController(ProductRepositoryTest productRepository, FallbackManager fallbackManager) {
        this.productRepository = productRepository;
        this.fallbackManager = fallbackManager;
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
                productsListView.getItems().setAll(products.stream().map(Product::getName).collect(Collectors.toList()));
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

}