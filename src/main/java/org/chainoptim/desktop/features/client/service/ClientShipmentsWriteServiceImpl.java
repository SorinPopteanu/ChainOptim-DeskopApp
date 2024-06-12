package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.client.dto.CreateClientShipmentDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientShipmentDTO;
import org.chainoptim.desktop.features.client.model.ClientShipment;
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

public class ClientShipmentsWriteServiceImpl implements ClientShipmentsWriteService {

    private final CachingService<PaginatedResults<ClientShipment>> cachingService;
    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public ClientShipmentsWriteServiceImpl(CachingService<PaginatedResults<ClientShipment>> cachingService,
                                           RequestBuilder requestBuilder,
                                           RequestHandler requestHandler,
                                           TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<ClientShipment>> createClientShipment(CreateClientShipmentDTO shipmentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/client-shipments/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), shipmentDTO);

        return requestHandler.sendRequest(request, new TypeReference<ClientShipment>() {}, shipment -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<ClientShipment>>> createClientShipmentsInBulk(List<CreateClientShipmentDTO> shipmentDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/client-shipments/create/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), shipmentDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<ClientShipment>>() {}, shipments -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<ClientShipment>>> updateClientShipmentsInBulk(List<UpdateClientShipmentDTO> shipmentDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/client-shipments/update/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), shipmentDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<ClientShipment>>() {}, shipments -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<Integer>>> deleteClientShipmentInBulk(List<Integer> shipmentIds) {
        String routeAddress = "http://localhost:8080/api/v1/client-shipments/delete/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), shipmentIds);

        return requestHandler.sendRequest(request, new TypeReference<List<Integer>>() {}, ids -> {
            cachingService.clear(); // Invalidate cache
        });
    }
}
