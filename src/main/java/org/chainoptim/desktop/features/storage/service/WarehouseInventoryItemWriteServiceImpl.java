package org.chainoptim.desktop.features.storage.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.production.model.FactoryInventoryItem;
import org.chainoptim.desktop.features.storage.dto.CreateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.storage.dto.UpdateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.storage.model.WarehouseInventoryItem;
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

public class WarehouseInventoryItemWriteServiceImpl implements WarehouseInventoryItemWriteService {

    private final CachingService<PaginatedResults<FactoryInventoryItem>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public WarehouseInventoryItemWriteServiceImpl(CachingService<PaginatedResults<FactoryInventoryItem>> cachingService,
                                                  RequestHandler requestHandler,
                                                  RequestBuilder requestBuilder,
                                                  TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<WarehouseInventoryItem>> createWarehouseInventoryItem(CreateWarehouseInventoryItemDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<WarehouseInventoryItem>() {});
    }

    public CompletableFuture<Result<WarehouseInventoryItem>> updateWarehouseInventoryItem(UpdateWarehouseInventoryItemDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<WarehouseInventoryItem>() {});
    }

    public CompletableFuture<Result<Integer>> deleteWarehouseInventoryItem(Integer orderId) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/delete/" + orderId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }

    public CompletableFuture<Result<List<Integer>>> deleteWarehouseInventoryItemsInBulk(List<Integer> itemIds) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/delete/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), itemIds);

        return requestHandler.sendRequest(request, new TypeReference<List<Integer>>() {
        }, ids -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<WarehouseInventoryItem>>> createWarehouseInventoryItemsInBulk(List<CreateWarehouseInventoryItemDTO> itemDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/create/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), itemDTOs);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<List<WarehouseInventoryItem>>() {});
    }

    public CompletableFuture<Result<List<WarehouseInventoryItem>>> updateWarehouseInventoryItemsInBulk(List<UpdateWarehouseInventoryItemDTO> itemDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/warehouse-inventory-items/update/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), itemDTOs);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<List<WarehouseInventoryItem>>() {});
    }
}
