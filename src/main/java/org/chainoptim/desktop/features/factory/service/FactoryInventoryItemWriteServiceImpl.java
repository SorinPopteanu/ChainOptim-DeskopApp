package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.factory.dto.CreateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.factory.model.FactoryInventoryItem;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class FactoryInventoryItemWriteServiceImpl implements FactoryInventoryItemWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public FactoryInventoryItemWriteServiceImpl(RequestHandler requestHandler,
                                                RequestBuilder requestBuilder,
                                                TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<FactoryInventoryItem>> createFactoryInventoryItem(CreateFactoryInventoryItemDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<FactoryInventoryItem>() {});
    }

    public CompletableFuture<Result<FactoryInventoryItem>> updateFactoryInventoryItem(UpdateFactoryInventoryItemDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<FactoryInventoryItem>() {});
    }

    public CompletableFuture<Result<Integer>> deleteFactoryInventoryItem(Integer orderId) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/delete/" + orderId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
