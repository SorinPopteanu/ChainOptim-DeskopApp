package org.chainoptim.desktop.features.productpipeline.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.productpipeline.dto.ComponentsSearchDTO;
import org.chainoptim.desktop.features.productpipeline.dto.CreateComponentDTO;
import org.chainoptim.desktop.features.productpipeline.dto.UpdateComponentDTO;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ComponentServiceImpl implements ComponentService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public ComponentServiceImpl(RequestHandler requestHandler,
                                RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    // Fetch
    public CompletableFuture<Result<List<Component>>> getComponentsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/components/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<Component>>() {});
    }

    @Override
    public CompletableFuture<Result<List<ComponentsSearchDTO>>> getComponentsByOrganizationIdSmall(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/components/organization/" + organizationId.toString() + "/small";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<ComponentsSearchDTO>>() {});
    }

    // Create
    public CompletableFuture<Result<Component>> createComponent(CreateComponentDTO componentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/components/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, TokenManager.getToken(), componentDTO);

        return requestHandler.sendRequest(request, new TypeReference<Component>() {});
    }

    // Update
    public CompletableFuture<Result<Component>> updateComponent(UpdateComponentDTO componentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/components/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, TokenManager.getToken(), componentDTO);

        return requestHandler.sendRequest(request, new TypeReference<Component>() {});
    }

    // Delete
    public CompletableFuture<Result<Integer>> deleteComponent(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/components/delete/" + id.toString();

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, TokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
