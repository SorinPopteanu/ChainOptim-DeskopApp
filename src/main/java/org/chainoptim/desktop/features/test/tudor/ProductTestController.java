package org.chainoptim.desktop.features.test.tudor;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductTestController implements Initializable {

    private final ProductRepositoryTest productRepository;
    private final CurrentSelectionService currentSelectionService;
    private final FallbackManager fallbackManager;

    private Product product;

    @FXML
    private StackPane fallbackContainer;

    @Inject
    public ProductTestController(ProductRepositoryTest productRepository,
                                 FallbackManager fallbackManager,
                                 CurrentSelectionService currentSelectionService) {
        this.productRepository = productRepository;
        this.fallbackManager = fallbackManager;
        this.currentSelectionService = currentSelectionService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer productId = currentSelectionService.getSelectedId();
        if (productId == null) {
            System.out.println("Failed to load product");
            fallbackManager.setErrorMessage("Failed to load product.");
        }

        loadProduct();
    }

    private void loadProduct() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }

        Integer organizationId = currentUser.getOrganization().getId();
        fallbackManager.setLoading(true);

        productRepository.getProductWithStages(organizationId)
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
            System.out.println("Product: " + product);
        });

        return productOptional;
    }

    private Optional<Product> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load product."));
        return Optional.empty();
    }


}
