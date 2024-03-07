package org.chainoptim.desktop.features.product.repository;

import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProductRepository {

    public CompletableFuture<Optional<List<Product>>> getProductsByOrganizationId(Integer organizationId);

    public CompletableFuture<Optional<PaginatedResults<Product>>> getProductsByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending,
            int page,
            int itemsPerPage
    );
    public CompletableFuture<Optional<Product>> getProductWithStages(Integer productId);
}
