package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.product.dto.ProductOverviewDTO;
import org.chainoptim.desktop.features.product.dto.ProductsSearchDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProductServiceImpl implements ProductService {

    private final CachingService<PaginatedResults<Product>> cachingService;
    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;

    private static final int STALE_TIME = 30000;

    @Inject
    public ProductServiceImpl(CachingService<PaginatedResults<Product>> cachingService,
                              RequestBuilder requestBuilder,
                              RequestHandler requestHandler) {
        this.cachingService = cachingService;
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
    }

    public CompletableFuture<Result<List<ProductsSearchDTO>>> getProductsByOrganizationId(Integer organizationId, boolean small) {
        String routeAddress = "http://localhost:8080/api/v1/products/organization/" + organizationId.toString() + (small ? "/small" : "");

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());
        if (request == null) {
            return requestHandler.getParsingErrorResult();
        }

        return requestHandler.sendRequest(request, new TypeReference<List<ProductsSearchDTO>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<Product>>> getProductsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("products", "organization", organizationId.toString(), searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());
        if (request == null) {
            return requestHandler.getParsingErrorResult();
        }

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<Product>>() {}, products -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, products, STALE_TIME);
        });
    }

    public CompletableFuture<Result<Product>> getProductWithStages(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/products/" + productId.toString() + "/stages";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());
        if (request == null) {
            return requestHandler.getParsingErrorResult();
        }

        return requestHandler.sendRequest(request, new TypeReference<Product>() {});
    }

    public CompletableFuture<Result<ProductOverviewDTO>> getProductOverview(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/products/" + productId.toString() + "/overview";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());
        if (request == null) {
            return requestHandler.getParsingErrorResult();
        }

        return requestHandler.sendRequest(request, new TypeReference<ProductOverviewDTO>() {});
    }
}
