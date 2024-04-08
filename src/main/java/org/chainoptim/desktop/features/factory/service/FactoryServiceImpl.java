package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.factory.dto.FactoriesSearchDTO;
import org.chainoptim.desktop.features.factory.model.Factory;
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

public class FactoryServiceImpl implements FactoryService {

    private final CachingService<PaginatedResults<Factory>> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 300;

    @Inject
    public FactoryServiceImpl(CachingService<PaginatedResults<Factory>> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<List<FactoriesSearchDTO>>> getFactoriesByOrganizationIdSmall(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/factories/organization/" + organizationId.toString() + "/small";

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<FactoriesSearchDTO>>empty();
                    try {
                        List<FactoriesSearchDTO> factories = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<FactoriesSearchDTO>>() {});
                        return Optional.of(factories);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<FactoriesSearchDTO>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<List<Factory>>> getFactoriesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/factories/organizations/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Factory>>empty();
                    try {
                        List<Factory> factories = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Factory>>() {});
                        return Optional.of(factories);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Factory>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<Factory>>> getFactoriesByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("products", organizationId, searchParams);
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Factory>>empty();
                    try {
                        PaginatedResults<Factory> factories = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Factory>>() {});

                        cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
                        cachingService.add(cacheKey, factories, STALE_TIME);

                        return Optional.of(factories);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Factory>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Factory>> getFactoryById(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/v1/factories/" + factoryId.toString();

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
                        Factory factory = JsonUtil.getObjectMapper().readValue(response.body(), Factory.class);
                        return Optional.of(factory);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
