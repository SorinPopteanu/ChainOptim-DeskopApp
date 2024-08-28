package org.chainoptim.desktop.core.overview.notification.service;

import org.chainoptim.desktop.core.overview.notification.model.NotificationUser;
import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
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

public class NotificationPersistenceServiceImpl implements NotificationPersistenceService {

    private final CachingService<PaginatedResults<NotificationUser>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    private static final int STALE_TIME = 300;

    @Inject
    public NotificationPersistenceServiceImpl(
            CachingService<PaginatedResults<NotificationUser>> cachingService,
            RequestHandler requestHandler,
            RequestBuilder requestBuilder,
            TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<NotificationUser>>> getNotificationsByUserId(String userId) {
        String routeAddress = "http://localhost:8080/api/v1/notifications/user/" + userId;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<NotificationUser>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<NotificationUser>>> getNotificationsByUserIdAdvanced(String userId, SearchParams searchParams) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("notifications", "user", userId, searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        if (cachingService.isCached(cacheKey) && !cachingService.isStale(cacheKey)) {
            return CompletableFuture.completedFuture(new Result<>(cachingService.get(cacheKey), null, HttpURLConnection.HTTP_OK));
        }

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<NotificationUser>>() {}, notifications -> {
            cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
            cachingService.add(cacheKey, notifications, STALE_TIME);
        });
    }
}
