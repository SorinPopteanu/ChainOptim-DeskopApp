package org.chainoptim.desktop.features.scanalysis.productionhistory.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.productionhistory.dto.AddDayToFactoryProductionHistoryDTO;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class FactoryProductionHistoryServiceImpl implements FactoryProductionHistoryService {

    private final CachingService<FactoryProductionHistory> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    private static final int STALE_TIME = 300;

    @Inject
    public FactoryProductionHistoryServiceImpl(CachingService<FactoryProductionHistory> cachingService,
                                               RequestHandler requestHandler,
                                               RequestBuilder requestBuilder) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<FactoryProductionHistory>> getFactoryProductionHistoryByFactoryId(Integer factoryId) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("factory-production-histories", "factory", factoryId);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<FactoryProductionHistory>() {}, productionHistory -> {
            cachingService.remove(cacheKey);
            cachingService.add(cacheKey, productionHistory, STALE_TIME);
        });
    }

    public CompletableFuture<Result<FactoryProductionHistory>> addDayToFactoryProductionHistory(AddDayToFactoryProductionHistoryDTO addDayDTO) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("factory-production-histories", "factory", addDayDTO.getFactoryId());
        String routeAddress = rootAddress + "factory-production-histories/add-day";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, TokenManager.getToken(), addDayDTO);

        return requestHandler.sendRequest(request, new TypeReference<FactoryProductionHistory>() {}, productionHistory -> {
            cachingService.remove(cacheKey);
            cachingService.add(cacheKey, productionHistory, STALE_TIME);
        });
    }
}
