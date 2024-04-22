package org.chainoptim.desktop.features.warehouse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class WarehouseWriteServiceImpl implements WarehouseWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public WarehouseWriteServiceImpl(RequestHandler requestHandler,
                                     RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<Warehouse>> createWarehouse(CreateWarehouseDTO warehouseDTO) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, TokenManager.getToken(), warehouseDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Warehouse>() {});
    }

    public CompletableFuture<Result<Warehouse>> updateWarehouse(UpdateWarehouseDTO warehouseDTO) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, TokenManager.getToken(), warehouseDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Warehouse>() {});
    }

    public CompletableFuture<Result<Integer>> deleteWarehouse(Integer warehouseId) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/delete/" + warehouseId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, TokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
