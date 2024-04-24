package org.chainoptim.desktop.features.supplier.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
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

public class SupplierOrdersServiceImpl implements SupplierOrdersService {

    private final CachingService<PaginatedResults<SupplierOrder>> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 300;

    @Inject
    public SupplierOrdersServiceImpl(CachingService<PaginatedResults<SupplierOrder>> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<List<SupplierOrder>>> getSupplierOrdersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK)
                        return Optional.<List<SupplierOrder>>empty();
                    try {
                        List<SupplierOrder> orders = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<SupplierOrder>>() {
                        });
                        return Optional.of(orders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<SupplierOrder>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<SupplierOrder>>> getSuppliersBySupplierIdAdvanced(
            Integer supplierId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("supplier-orders", "organization", supplierId.toString(), searchParams);
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                        return Optional.<PaginatedResults<SupplierOrder>>empty();
                    }
                    try {
                        PaginatedResults<SupplierOrder> supplierOrders = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<SupplierOrder>>() {});

                        cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
                        cachingService.add(cacheKey, supplierOrders, STALE_TIME);

                        return Optional.of(supplierOrders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<SupplierOrder>>empty();
                    }
                });
    }

}
