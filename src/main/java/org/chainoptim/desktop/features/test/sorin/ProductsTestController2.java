package org.chainoptim.desktop.features.test.sorin;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
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

    @FXML
    private TilePane productsTilePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (TenantContext.getCurrentUser() != null) {

            Integer organizationId = TenantContext.getCurrentUser().getOrganization().getId();
            Optional<List<Product>> products = productRepository.getProductsByOrganizationId(organizationId);

            if (products.isPresent()) {
                for (Product product : products.get()) {
                    Label productName = new Label(product.getName());
                    productName.getStyleClass().add("name-label");
                    Label productDescription = new Label(product.getDescription());
                    productDescription.getStyleClass().add("description-label");
                    VBox productBox = new VBox(productName, productDescription);
                    Button productButton = new Button();
                    productButton.setGraphic(productBox);

                    productsTilePane.getChildren().add(productButton);
                }
            }
        }

    }

}
