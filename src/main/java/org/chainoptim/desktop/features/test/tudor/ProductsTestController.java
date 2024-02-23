package org.chainoptim.desktop.features.test.tudor;


import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.repository.ProductRepository;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductsTestController implements Initializable {

    private final ProductRepository productRepository;
    private final FallbackManager fallbackManager;

    @FXML
    private StackPane fallbackContainer;

    @Inject
    public ProductsTestController(ProductRepository productRepository, FallbackManager fallbackManager) {
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
        fallbackManager.setLoading(true);

        if (TenantContext.getCurrentUser() != null) {
            Integer organizationId = TenantContext.getCurrentUser().getOrganization().getId();

            // Attempt to fetch organization products
            try {
                Optional<List<Product>> products = productRepository.getProductsByOrganizationId(organizationId);
                if (products.isPresent()) {
                    fallbackManager.setLoading(false);
                    if (products.get().isEmpty()) {
                        fallbackManager.setNoResults(true);
                    }
                } else {
                    fallbackManager.setErrorMessage("Failed to load products.");
                }
                System.out.println(products);
            } catch (Exception e) {
                fallbackManager.setErrorMessage("Failed to load products.");
            } finally {
                fallbackManager.setLoading(false);
            }
        }
        System.out.println(fallbackManager.isLoading());
    }

}