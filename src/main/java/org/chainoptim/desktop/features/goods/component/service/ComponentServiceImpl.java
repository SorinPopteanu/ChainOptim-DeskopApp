package org.chainoptim.desktop.features.goods.component.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.goods.component.dto.ComponentsSearchDTO;
import org.chainoptim.desktop.features.goods.component.dto.CreateComponentDTO;
import org.chainoptim.desktop.features.goods.component.dto.UpdateComponentDTO;
import org.chainoptim.desktop.features.goods.component.model.Component;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ComponentServiceImpl implements ComponentService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public ComponentServiceImpl(RequestHandler requestHandler,
                                RequestBuilder requestBuilder,
                                TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    // Fetch
    public CompletableFuture<Result<List<Component>>> getComponentsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/components/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<Component>>() {});
    }

    @Override
    public CompletableFuture<Result<List<ComponentsSearchDTO>>> getComponentsByOrganizationIdSmall(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/components/organization/" + organizationId.toString() + "/small";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<ComponentsSearchDTO>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<Component>>> getComponentsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String rootAddress = "http://localhost:8080/api/v1/";
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("components", "organization", organizationId.toString(), searchParams);
        String routeAddress = rootAddress + cacheKey;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<Component>>() {});
    }

    public CompletableFuture<Result<Component>> getComponentById(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/components/" + id.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<Component>() {});
    }

    // Create
    public CompletableFuture<Result<Component>> createComponent(CreateComponentDTO componentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/components/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), componentDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Component>() {});
    }

    // Update
    public CompletableFuture<Result<Component>> updateComponent(UpdateComponentDTO componentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/components/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), componentDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Component>() {});
    }

    // Delete
    public CompletableFuture<Result<Integer>> deleteComponent(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/components/delete/" + id.toString();

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
