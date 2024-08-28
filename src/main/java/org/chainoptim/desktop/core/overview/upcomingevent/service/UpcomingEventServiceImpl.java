package org.chainoptim.desktop.core.overview.upcomingevent.service;

import org.chainoptim.desktop.core.overview.upcomingevent.model.UpcomingEvent;
import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UpcomingEventServiceImpl implements UpcomingEventService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public UpcomingEventServiceImpl(RequestHandler requestHandler,
                                    RequestBuilder requestBuilder,
                                    TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<UpcomingEvent>>> getUpcomingEventsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/upcoming-events/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<UpcomingEvent>>() {});
    }

    public CompletableFuture<Result<List<UpcomingEvent>>> getUpcomingEventsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey(
                "upcoming-events", "organization", organizationId.toString(), searchParams);
        String routeAddress = rootAddress + cacheKey;
        System.out.println("Route address: " + routeAddress);

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<UpcomingEvent>>() {});
    }
}
