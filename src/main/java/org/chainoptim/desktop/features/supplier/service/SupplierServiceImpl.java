package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.supplier.model.Supplier;
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

public class SupplierServiceImpl implements SupplierService {

    private final CachingService<PaginatedResults<Supplier>> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 300;

    @Inject
    public SupplierServiceImpl(CachingService<PaginatedResults<Supplier>> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<List<Supplier>>> getSuppliersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Supplier>>empty();
                    try {
                        List<Supplier> suppliers = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Supplier>>() {});
                        return Optional.of(suppliers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Supplier>>empty();
                    }
                });
    }


    public CompletableFuture<Optional<PaginatedResults<Supplier>>> getSuppliersByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("suppliers", organizationId, searchParams);
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Supplier>>empty();
                    try {
                        PaginatedResults<Supplier> suppliers = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Supplier>>() {});

                        cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
                        cachingService.add(cacheKey, suppliers, STALE_TIME);

                        return Optional.of(suppliers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Supplier>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Supplier>> getSupplierById(Integer supplierId) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/" + supplierId.toString();

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
                        Supplier supplier = JsonUtil.getObjectMapper().readValue(response.body(), Supplier.class);
                        return Optional.of(supplier);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
