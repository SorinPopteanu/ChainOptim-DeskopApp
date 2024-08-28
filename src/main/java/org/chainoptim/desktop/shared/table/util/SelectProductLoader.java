package org.chainoptim.desktop.shared.table.util;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.product.dto.ProductsSearchDTO;
import org.chainoptim.desktop.features.goods.product.service.ProductService;
import org.chainoptim.desktop.shared.httphandling.Result;
import com.google.inject.Inject;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectProductLoader {


    private final ProductService productService;
    private final Map<Integer, String> productsMap = new HashMap<>();

    @Inject
    public SelectProductLoader(ProductService productService) {
        this.productService = productService;
    }

    public void initialize() {
        loadProducts();
    }

    public List<String> getProductsName() {
        return new ArrayList<>(productsMap.values());
    }

    public Integer getProductIdByName(String name) {
        for (Map.Entry<Integer, String> entry : productsMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void loadProducts() {
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
            productsMap.clear();
            result.getData().forEach(product -> productsMap.put(product.getId(), product.getName()));
        });
        return result;
    }

    private Result<List<ProductsSearchDTO>> handleProductsException(Throwable ex) {
        return new Result<>();
    }
}
