package org.chainoptim.desktop.features.supply.supplier.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.supply.supplier.dto.SupplierOverviewDTO;
import org.chainoptim.desktop.features.supply.supplier.model.Supplier;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
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

public class SupplierServiceImpl implements SupplierService {

    private final CachingService<PaginatedResults<Supplier>> cachingService;
    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    private static final int STALE_TIME = 300;

    @Inject
    public SupplierServiceImpl(CachingService<PaginatedResults<Supplier>> cachingService,
                               RequestBuilder requestBuilder,
                               RequestHandler requestHandler,
                               TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<Supplier>>> getSuppliersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<Supplier>>() {});
    }


    public CompletableFuture<Result<PaginatedResults<Supplier>>> getSuppliersByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("suppliers", "organization", organizationId.toString(), searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<Supplier>>() {}, suppliers -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, suppliers, STALE_TIME);
        });
    }

    public CompletableFuture<Result<Supplier>> getSupplierById(Integer supplierId) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/" + supplierId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<Supplier>() {});
    }

    public CompletableFuture<Result<SupplierOverviewDTO>> getSupplierOverview(Integer supplierId) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/" + supplierId.toString() + "/overview";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<SupplierOverviewDTO>() {});
    }
}
