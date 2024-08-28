package org.chainoptim.desktop.features.production.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.production.dto.CreateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.production.dto.UpdateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.production.model.FactoryInventoryItem;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FactoryInventoryItemWriteServiceImpl implements FactoryInventoryItemWriteService {

    private final CachingService<PaginatedResults<FactoryInventoryItem>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
public FactoryInventoryItemWriteServiceImpl(CachingService<PaginatedResults<FactoryInventoryItem>> cachingService,
                                            RequestHandler requestHandler,
                                            RequestBuilder requestBuilder,
                                            TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<FactoryInventoryItem>> createFactoryInventoryItem(CreateFactoryInventoryItemDTO itemDTO) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), itemDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<FactoryInventoryItem>() {});
    }

    public CompletableFuture<Result<FactoryInventoryItem>> updateFactoryInventoryItem(UpdateFactoryInventoryItemDTO itemDTO) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), itemDTO);
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

    public CompletableFuture<Result<List<FactoryInventoryItem>>> createFactoryInventoryItemsInBulk(List<CreateFactoryInventoryItemDTO> itemDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/create/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), itemDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<FactoryInventoryItem>>() {}, orders -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<FactoryInventoryItem>>> updateFactoryInventoryItemsInBulk(List<UpdateFactoryInventoryItemDTO> itemDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/update/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), itemDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<FactoryInventoryItem>>() {}, orders -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<Integer>>> deleteFactoryInventoryItemsInBulk(List<Integer> itemIds) {
        String routeAddress = "http://localhost:8080/api/v1/factory-inventory-items/delete/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), itemIds);

        return requestHandler.sendRequest(request, new TypeReference<List<Integer>>() {}, ids -> {
            cachingService.clear(); // Invalidate cache
        });
    }
}
