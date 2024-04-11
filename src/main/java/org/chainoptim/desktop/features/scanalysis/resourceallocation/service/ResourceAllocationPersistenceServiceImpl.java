package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.dto.UpdateAllocationPlanDTO;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.google.inject.Inject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ResourceAllocationPersistenceServiceImpl implements ResourceAllocationPersistenceService {

    private final CachingService<ResourceAllocationPlan> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 30000;

    @Inject
    public ResourceAllocationPersistenceServiceImpl(CachingService<ResourceAllocationPlan> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<ResourceAllocationPlan>> getResourceAllocationPlanByFactoryId(Integer factoryId) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("active-resource-allocation-plans", "factory", factoryId);
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
            System.out.println("Cache hit:  " + cacheKey + " - ResourceAllocationPlan");
            return CompletableFuture.completedFuture(Optional.of(cachingService.get(cacheKey)));
        }

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<ResourceAllocationPlan>empty();
                    try {
                        ResourceAllocationPlan allocationPlan = JsonUtil.getObjectMapper().readValue(response.body(), ResourceAllocationPlan.class);

                        cachingService.remove(cacheKey);
                        cachingService.add(cacheKey, allocationPlan, STALE_TIME);

                        return Optional.of(allocationPlan);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<ResourceAllocationPlan>empty();
                    }
                });
    }

    public CompletableFuture<Optional<ResourceAllocationPlan>> updateAllocationPlan(UpdateAllocationPlanDTO allocationPlanDTO) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("active-resource-allocation-plans", "factory", allocationPlanDTO.getFactoryId());
        String routeAddress = rootAddress + "active-resource-allocation-plans/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(allocationPlanDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers(HEADER_KEY, headerValue)
                .headers("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response: " + response);
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        ResourceAllocationPlan allocationPlan = JsonUtil.getObjectMapper().readValue(response.body(), ResourceAllocationPlan.class);

                        cachingService.remove(cacheKey); // Invalidate cache

                        return Optional.of(allocationPlan);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<ResourceAllocationPlan>empty();
                    }
                });
    }
}
