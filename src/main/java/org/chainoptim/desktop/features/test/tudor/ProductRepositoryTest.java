package org.chainoptim.desktop.features.test.tudor;

import org.chainoptim.desktop.features.product.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProductRepositoryTest {

    public CompletableFuture<Optional<List<Product>>> getProductsByOrganizationId(Integer organizationId);
    public CompletableFuture<Optional<Product>> getProductWithStages(Integer productId);
}
