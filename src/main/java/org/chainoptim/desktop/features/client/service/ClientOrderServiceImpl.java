package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.client.model.ClientOrder;
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

public class ClientOrderServiceImpl implements ClientOrderService {

    private final CachingService<PaginatedResults<ClientOrder>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    private static final int STALE_TIME = 30000;

    @Inject
    public ClientOrderServiceImpl(CachingService<PaginatedResults<ClientOrder>> cachingService,
                                  RequestHandler requestHandler,
                                  RequestBuilder requestBuilder,
                                  TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<ClientOrder>>> getClientOrdersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<List<ClientOrder>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<ClientOrder>>> getClientOrdersByClientIdAdvanced(
            Integer clientId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("client-orders", "client", clientId.toString(), searchParams);

        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<ClientOrder>>() {}, clientOrders -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, clientOrders, STALE_TIME);
        });
    }

    public CompletableFuture<Result<ClientOrder>> getClientOrderById(Integer orderId) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/" + orderId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<ClientOrder>() {});
    }
}
