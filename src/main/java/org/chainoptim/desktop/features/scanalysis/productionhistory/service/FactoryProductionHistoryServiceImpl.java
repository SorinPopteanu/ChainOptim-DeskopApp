package org.chainoptim.desktop.features.scanalysis.productionhistory.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.productionhistory.dto.AddDayToFactoryProductionHistoryDTO;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.supply.model.SupplierPerformance;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.google.inject.Inject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FactoryProductionHistoryServiceImpl implements FactoryProductionHistoryService {

    private final CachingService<FactoryProductionHistory> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 30000;

    @Inject
    public FactoryProductionHistoryServiceImpl(CachingService<FactoryProductionHistory> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<FactoryProductionHistory>> getFactoryProductionHistoryByFactoryId(Integer factoryId) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("factory-production-histories", "factory", factoryId);
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
            System.out.println("Cache hit:  " + cacheKey + " - FactoryProductionHistory");
            return CompletableFuture.completedFuture(Optional.of(cachingService.get(cacheKey)));
        }

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<FactoryProductionHistory>empty();
                    try {
                        FactoryProductionHistory productionHistory = JsonUtil.getObjectMapper().readValue(response.body(), FactoryProductionHistory.class);

                        cachingService.remove(cacheKey);
                        cachingService.add(cacheKey, productionHistory, STALE_TIME);

                        return Optional.of(productionHistory);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<FactoryProductionHistory>empty();
                    }
                });
    }

    public CompletableFuture<Optional<FactoryProductionHistory>> addDayToFactoryProductionHistory(AddDayToFactoryProductionHistoryDTO addDayDTO) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("factory-production-histories", "factory", addDayDTO.getFactoryId());
        String routeAddress = rootAddress + "factory-production-histories/add-day";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(addDayDTO);
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        FactoryProductionHistory productionHistory = JsonUtil.getObjectMapper().readValue(response.body(), FactoryProductionHistory.class);

                        cachingService.remove(cacheKey); // Invalidate cache

                        return Optional.of(productionHistory);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<FactoryProductionHistory>empty();
                    }
                });
    }
}
