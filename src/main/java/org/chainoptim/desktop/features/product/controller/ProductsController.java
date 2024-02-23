package org.chainoptim.desktop.features.product.controller;

import com.google.inject.Inject;
import javafx.fxml.Initializable;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.repository.ProductRepository;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductsController implements Initializable {

    @Inject
    private ProductRepository productRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TenantContext.currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                Integer organizationId = newUser.getOrganization().getId();
                Optional<List<Product>> products = productRepository.getProductsByOrganizationId(organizationId);
                System.out.println(products);
            }
        });

    }

}
