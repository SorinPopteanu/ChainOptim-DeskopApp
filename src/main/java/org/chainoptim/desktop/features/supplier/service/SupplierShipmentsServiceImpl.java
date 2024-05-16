package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.supplier.model.SupplierShipment;
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

public class SupplierShipmentsServiceImpl implements SupplierShipmentsService {

    private final CachingService<PaginatedResults<SupplierShipment>> cachingService;
    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;
    private static final int STALE_TIME = 300;

    @Inject
    public SupplierShipmentsServiceImpl(CachingService<PaginatedResults<SupplierShipment>> cachingService,
                                        RequestBuilder requestBuilder,
                                        RequestHandler requestHandler,
                                        TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<SupplierShipment>>> getSupplierShipmentsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-shipments/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<SupplierShipment>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<SupplierShipment>>> getSupplierShipmentsAdvanced(
            Integer entityId,
            SearchMode searchMode,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey(
                "supplier-shipments",
                searchMode == SearchMode.ORGANIZATION ? "organization" : "order", entityId.toString(),
                searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<SupplierShipment>>() {}, supplierShipments -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, supplierShipments, STALE_TIME);
        });
    }

}
