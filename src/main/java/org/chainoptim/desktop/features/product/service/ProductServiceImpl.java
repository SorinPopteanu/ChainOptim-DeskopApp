package org.chainoptim.desktop.features.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.product.dto.ProductsSearchDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.empty;

public class ProductServiceImpl implements ProductService {

    private final CachingService<PaginatedResults<Product>> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 30000;

    @Inject
    public ProductServiceImpl(CachingService<PaginatedResults<Product>> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<List<ProductsSearchDTO>>> getProductsByOrganizationId(Integer organizationId, boolean small) {
        String routeAddress = "http://localhost:8080/api/v1/products/organization/" + organizationId.toString() + (small ? "/small" : "");

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<ProductsSearchDTO>>empty();
                    try {
                        List<ProductsSearchDTO> products = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<ProductsSearchDTO>>() {});
                        return Optional.of(products);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<ProductsSearchDTO>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<Product>>> getProductsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("products", "organization", organizationId, searchParams);
        String routeAddress = rootAddress + cacheKey;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(Optional.of(cachingService.get(cacheKey)));
        }

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Product>>empty();
                    try {
                        PaginatedResults<Product> products = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Product>>() {});

                        cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
                        cachingService.add(cacheKey, products, STALE_TIME);

                        return Optional.of(products);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Product>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Product>> getProductWithStages(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/products/" + productId.toString() + "/stages";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return empty();
                    try {
                        Product product = JsonUtil.getObjectMapper().readValue(response.body(), Product.class);
                        return Optional.of(product);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return empty();
                    }
                });
    }
}
