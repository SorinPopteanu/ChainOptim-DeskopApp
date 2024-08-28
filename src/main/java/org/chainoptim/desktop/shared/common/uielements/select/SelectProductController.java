package org.chainoptim.desktop.shared.common.uielements.select;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.product.dto.ProductsSearchDTO;
import org.chainoptim.desktop.features.goods.product.service.ProductService;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.List;

public class SelectProductController {

    private final ProductService productService;

    @FXML
    private ComboBox<ProductsSearchDTO> productComboBox;

    @Inject
    public SelectProductController(ProductService productService) {
        this.productService = productService;
    }

    public void initialize() {
        loadProducts();
    }

    public ProductsSearchDTO getSelectedProduct() {
        return productComboBox.getSelectionModel().getSelectedItem();
    }

    private void loadProducts() {
        productComboBox.setCellFactory(lv -> new ListCell<ProductsSearchDTO>() {
            @Override
            protected void updateItem(ProductsSearchDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        productComboBox.setButtonCell(new ListCell<ProductsSearchDTO>() {
            @Override
            protected void updateItem(ProductsSearchDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        Integer organizationId = currentUser.getOrganization().getId();
        productService.getProductsByOrganizationId(organizationId, true)
                .thenApply(this::handleProductsResponse)
                .exceptionally(this::handleProductsException);
    }

    private Result<List<ProductsSearchDTO>> handleProductsResponse(Result<List<ProductsSearchDTO>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            productComboBox.getItems().setAll(result.getData());

        });
        return result;
    }

    private Result<List<ProductsSearchDTO>> handleProductsException(Throwable ex) {
        return new Result<>();
    }
}
