package org.chainoptim.desktop.features.production.analysis.resourceallocation.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.production.analysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public ResourceAllocationServiceImpl(RequestHandler requestHandler,
                                         RequestBuilder requestBuilder,
                                         TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }


    public CompletableFuture<Result<AllocationPlan>> allocateFactoryResources(Integer factoryId, Float duration) {
        String routeAddress = "http://localhost:8080/api/v1/factories/allocate-resources/" + factoryId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), duration);

        return requestHandler.sendRequest(request, new TypeReference<AllocationPlan>() {});
    }
}
