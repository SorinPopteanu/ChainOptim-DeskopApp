package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.client.dto.ClientOverviewDTO;
import org.chainoptim.desktop.features.client.model.Client;
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

public class ClientServiceImpl implements ClientService {

    private final CachingService<PaginatedResults<Client>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    private static final int STALE_TIME = 300;

    @Inject
    public ClientServiceImpl(CachingService<PaginatedResults<Client>> cachingService,
                             RequestHandler requestHandler,
                             RequestBuilder requestBuilder,
                             TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<Client>>> getClientsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/clients/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<Client>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<Client>>> getClientsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("clients", "organization", organizationId.toString(), searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<Client>>() {}, clients -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, clients, STALE_TIME);
        });
    }

    public CompletableFuture<Result<Client>> getClientById(Integer clientId) {
        String routeAddress = "http://localhost:8080/api/v1/clients/" + clientId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<Client>() {});
    }

    public CompletableFuture<Result<ClientOverviewDTO>> getClientOverview(Integer clientId) {
        String routeAddress = "http://localhost:8080/api/v1/clients/" + clientId.toString() + "/overview";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<ClientOverviewDTO>() {});
    }
}
