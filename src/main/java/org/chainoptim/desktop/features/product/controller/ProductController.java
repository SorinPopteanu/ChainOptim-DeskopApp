package org.chainoptim.desktop.features.product.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    private final ProductService productService;
    private final CurrentSelectionService currentSelectionService;
    private final FallbackManager fallbackManager;

    private Product product;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private Label productName;

    @Inject
    public ProductController(ProductService productService,
                             FallbackManager fallbackManager,
                             CurrentSelectionService currentSelectionService) {
        this.productService = productService;
        this.fallbackManager = fallbackManager;
        this.currentSelectionService = currentSelectionService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer productId = currentSelectionService.getSelectedId();
        if (productId == null) {
            System.out.println("Missing product id.");
            fallbackManager.setErrorMessage("Failed to load product.");
        }

        loadProduct(productId);
    }

    private void loadProduct(Integer productId) {
        fallbackManager.setLoading(true);

        productService.getProductWithStages(productId)
                .thenApply(this::handleProductResponse)
                .exceptionally(this::handleProductException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<Product> handleProductResponse(Optional<Product> productOptional) {
        Platform.runLater(() -> {
            if (productOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load product.");
                return;
            }
            this.product = productOptional.get();
            productName.setText(product.getName());
            System.out.println("Product: " + product);
        });

        return productOptional;
    }

    private Optional<Product> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load product."));
        return Optional.empty();
    }


}
