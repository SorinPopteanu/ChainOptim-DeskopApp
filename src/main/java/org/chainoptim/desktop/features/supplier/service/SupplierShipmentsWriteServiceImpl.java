package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierShipmentDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierShipmentDTO;
import org.chainoptim.desktop.features.supplier.model.SupplierShipment;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SupplierShipmentsWriteServiceImpl implements SupplierShipmentsWriteService {

    private final CachingService<PaginatedResults<SupplierShipment>> cachingService;
    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public SupplierShipmentsWriteServiceImpl(CachingService<PaginatedResults<SupplierShipment>> cachingService,
                                             RequestBuilder requestBuilder,
                                             RequestHandler requestHandler,
                                             TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<SupplierShipment>> createSupplierShipment(CreateSupplierShipmentDTO shipmentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-shipments/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), shipmentDTO);

        return requestHandler.sendRequest(request, new TypeReference<SupplierShipment>() {}, shipment -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<SupplierShipment>>> createSupplierShipmentsInBulk(List<CreateSupplierShipmentDTO> shipmentDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-shipments/create/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), shipmentDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<SupplierShipment>>() {}, shipments -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<SupplierShipment>>> updateSupplierShipmentsInBulk(List<UpdateSupplierShipmentDTO> shipmentDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-shipments/update/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), shipmentDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<SupplierShipment>>() {}, shipments -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<Integer>>> deleteSupplierShipmentInBulk(List<Integer> shipmentIds) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-shipments/delete/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), shipmentIds);

        requestHandler.sendRequest(request, new TypeReference<>() {}, ids ->
            cachingService.clear() // Invalidate cache
        );

        return CompletableFuture.completedFuture(new Result<>(shipmentIds, null, HttpURLConnection.HTTP_OK));

    }
}
