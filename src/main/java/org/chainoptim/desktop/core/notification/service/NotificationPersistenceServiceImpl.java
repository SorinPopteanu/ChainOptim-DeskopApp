package org.chainoptim.desktop.core.notification.service;

import org.chainoptim.desktop.core.notification.model.Notification;
import org.chainoptim.desktop.core.notification.model.NotificationUser;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.factory.dto.FactoriesSearchDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NotificationPersistenceServiceImpl implements NotificationPersistenceService {

    private final CachingService<PaginatedResults<NotificationUser>> cachingService;
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";
    private static final int STALE_TIME = 30000;

    @Inject
    public NotificationPersistenceServiceImpl(CachingService<PaginatedResults<NotificationUser>> cachingService) {
        this.cachingService = cachingService;
    }

    public CompletableFuture<Optional<List<NotificationUser>>> getNotificationsByUserId(String userId) {
        String routeAddress = "http://localhost:8080/api/v1/notifications/user/" + userId;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<NotificationUser>>empty();
                    try {
                        List<NotificationUser> notifications = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<NotificationUser>>() {});
                        return Optional.of(notifications);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<NotificationUser>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<NotificationUser>>> getNotificationsByUserIdAdvanced(String userId, SearchParams searchParams) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("notifications", "user", userId, searchParams);
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
            return CompletableFuture.completedFuture(Optional.of(cachingService.get(cacheKey)));
        }

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<NotificationUser>>empty();
                    try {
                        PaginatedResults<NotificationUser> notifications = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<NotificationUser>>() {});

                        cachingService.remove(cacheKey); // Ensure there isn't a stale cache entry
                        cachingService.add(cacheKey, notifications, STALE_TIME);

                        return Optional.of(notifications);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<NotificationUser>>empty();
                    }
                });
    }
}
