package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.dto.UpdateAllocationPlanDTO;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
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

public class ResourceAllocationPersistenceServiceImpl implements ResourceAllocationPersistenceService {

    private final CachingService<ResourceAllocationPlan> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    private static final int STALE_TIME = 300;

    @Inject
    public ResourceAllocationPersistenceServiceImpl(CachingService<ResourceAllocationPlan> cachingService,
                                                    RequestHandler requestHandler,
                                                    RequestBuilder requestBuilder) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<ResourceAllocationPlan>> getResourceAllocationPlanByFactoryId(Integer factoryId) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("active-resource-allocation-plans", "factory", factoryId);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<ResourceAllocationPlan>() {}, allocationPlan -> {
            cachingService.remove(cacheKey);
            cachingService.add(cacheKey, allocationPlan, STALE_TIME);
        });
    }

    public CompletableFuture<Result<ResourceAllocationPlan>> updateAllocationPlan(UpdateAllocationPlanDTO allocationPlanDTO) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildSecondaryFeatureKey("active-resource-allocation-plans", "factory", allocationPlanDTO.getFactoryId());
        String routeAddress = rootAddress + "active-resource-allocation-plans/update";

        HttpRequest request = requestBuilder.buildWriteRequest(HttpMethod.PUT, routeAddress, TokenManager.getToken(), allocationPlanDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<ResourceAllocationPlan>() {}, allocationPlan -> {
            cachingService.remove(cacheKey);
            cachingService.add(cacheKey, allocationPlan, STALE_TIME);
        });
    }
}
