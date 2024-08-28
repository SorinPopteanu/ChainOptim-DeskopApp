package org.chainoptim.desktop.features.supply.supplierorder.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.supply.supplierorder.dto.CreateSupplierOrderDTO;
import org.chainoptim.desktop.features.supply.supplierorder.dto.UpdateSupplierOrderDTO;
import org.chainoptim.desktop.features.supply.supplierorder.model.SupplierOrder;
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

public class SupplierOrdersWriteServiceImpl implements SupplierOrdersWriteService {

    private final CachingService<PaginatedResults<SupplierOrder>> cachingService;
    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public SupplierOrdersWriteServiceImpl(CachingService<PaginatedResults<SupplierOrder>> cachingService,
                                          RequestBuilder requestBuilder,
                                          RequestHandler requestHandler,
                                          TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<SupplierOrder>> createSupplierOrder(CreateSupplierOrderDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), orderDTO);

        return requestHandler.sendRequest(request, new TypeReference<SupplierOrder>() {}, order -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<SupplierOrder>>> createSupplierOrdersInBulk(List<CreateSupplierOrderDTO> orderDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/create/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), orderDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<SupplierOrder>>() {}, orders -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<SupplierOrder>>> updateSupplierOrdersInBulk(List<UpdateSupplierOrderDTO> orderDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/update/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), orderDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<SupplierOrder>>() {}, orders -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<Integer>>> deleteSupplierOrderInBulk(List<Integer> orderIds) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/delete/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), orderIds);

        return requestHandler.sendRequest(request, new TypeReference<List<Integer>>() {}, ids -> {
            cachingService.clear(); // Invalidate cache
        });
    }
}
