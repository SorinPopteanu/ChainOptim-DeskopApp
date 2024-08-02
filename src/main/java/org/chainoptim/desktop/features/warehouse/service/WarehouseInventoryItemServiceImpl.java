package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.warehouse.model.WarehouseInventoryItem;
import org.chainoptim.desktop.features.warehouse.service.WarehouseInventoryItemService;
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

public class WarehouseInventoryItemServiceImpl implements WarehouseInventoryItemService {

    private final CachingService<PaginatedResults<WarehouseInventoryItem>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    private static final int STALE_TIME = 300;

    @Inject
    public WarehouseInventoryItemServiceImpl(CachingService<PaginatedResults<WarehouseInventoryItem>> cachingService,
                                             RequestHandler requestHandler,
                                             RequestBuilder requestBuilder,
                                             TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<WarehouseInventoryItem>>> getWarehouseInventoryItemsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<List<WarehouseInventoryItem>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<WarehouseInventoryItem>>> getWarehouseInventoryItemsByWarehouseIdAdvanced(
            Integer entityId,
            SearchParams searchParams,
            SearchMode searchMode
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey(
                "warehouse-inventory-items",
                searchMode == SearchMode.ORGANIZATION ? "organization" : "warehouse", entityId.toString(),
                searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<WarehouseInventoryItem>>() {}, warehouseInventoryItems -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, warehouseInventoryItems, STALE_TIME);
        });
    }

    public CompletableFuture<Result<WarehouseInventoryItem>> getWarehouseInventoryItemById(Integer itemId) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/" + itemId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<WarehouseInventoryItem>() {});
    }
}
