package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WarehouseServiceImpl implements WarehouseService {

    private final CachingService<PaginatedResults<Warehouse>> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 30000;

    @Inject
    public WarehouseServiceImpl(CachingService<PaginatedResults<Warehouse>> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<List<Warehouse>>> getWarehousesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Warehouse>>empty();
                    try {
                        List<Warehouse> warehouses = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Warehouse>>() {});
                        return Optional.of(warehouses);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Warehouse>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<Warehouse>>> getWarehousesByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("warehouses", organizationId, searchParams);
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Warehouse>>empty();
                    try {
                        PaginatedResults<Warehouse> warehouses = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Warehouse>>() {});

                        cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
                        cachingService.add(cacheKey, warehouses, STALE_TIME);

                        return Optional.of(warehouses);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Warehouse>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Warehouse>> getWarehouseById(Integer warehouseId) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/" + warehouseId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        Warehouse warehouse = JsonUtil.getObjectMapper().readValue(response.body(), Warehouse.class);
                        return Optional.of(warehouse);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
