package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.features.product.dto.ProductOverviewDTO;
import org.chainoptim.desktop.features.product.dto.ProductsSearchDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProductService {

    CompletableFuture<Optional<List<ProductsSearchDTO>>> getProductsByOrganizationId(Integer organizationId, boolean small);

    CompletableFuture<Optional<PaginatedResults<Product>>> getProductsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    );
    CompletableFuture<Optional<Product>> getProductWithStages(Integer productId);
    CompletableFuture<Optional<ProductOverviewDTO>> getProductOverview(Integer productId);
}
