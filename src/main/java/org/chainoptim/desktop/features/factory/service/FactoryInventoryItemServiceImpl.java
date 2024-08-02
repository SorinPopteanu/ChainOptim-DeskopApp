package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.factory.model.FactoryInventoryItem;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.enums.SearchMode;
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

public class FactoryInventoryItemServiceImpl implements FactoryInventoryItemService {

    private final CachingService<PaginatedResults<FactoryInventoryItem>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    private static final int STALE_TIME = 300;

    @Inject
    public FactoryInventoryItemServiceImpl(CachingService<PaginatedResults<FactoryInventoryItem>> cachingService,
                                           RequestHandler requestHandler,
                                           RequestBuilder requestBuilder,
                                           TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<FactoryInventoryItem>>> getFactoryInventoryItemsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/factory/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<List<FactoryInventoryItem>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<FactoryInventoryItem>>> getFactoryInventoryItemsByFactoryIdAdvanced(
            Integer entityId,
            SearchParams searchParams,
            SearchMode searchMode
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey(
                "factory-inventory-items",
                searchMode == SearchMode.ORGANIZATION ? "organization" : "factory", entityId.toString(),
                searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<FactoryInventoryItem>>() {}, factoryInventoryItems -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, factoryInventoryItems, STALE_TIME);
        });
    }

    public CompletableFuture<Result<FactoryInventoryItem>> getFactoryInventoryItemById(Integer itemId) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/" + itemId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<FactoryInventoryItem>() {});
    }
}
