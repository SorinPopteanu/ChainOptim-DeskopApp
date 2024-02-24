package org.chainoptim.desktop.features.test.sorin;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.repository.ProductRepository;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductsTestController2 implements Initializable {

    private final ProductRepository productRepository;

    @Inject
    public ProductsTestController2(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (TenantContext.getCurrentUser() != null) {
            Integer organizationId = TenantContext.getCurrentUser().getOrganization().getId();
            System.out.println(organizationId);
            Optional<List<Product>> products = productRepository.getProductsByOrganizationId(organizationId);
            System.out.println(products);
        }

    }

}
