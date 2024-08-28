package org.chainoptim.desktop.features.demand.clientorder.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.demand.clientorder.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.demand.clientorder.dto.UpdateClientOrderDTO;
import org.chainoptim.desktop.features.demand.clientorder.model.ClientOrder;
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

public class ClientOrdersWriteServiceImpl implements ClientOrdersWriteService {

    private final CachingService<PaginatedResults<ClientOrder>> cachingService;
    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public ClientOrdersWriteServiceImpl(CachingService<PaginatedResults<ClientOrder>> cachingService,
                                        RequestBuilder requestBuilder,
                                        RequestHandler requestHandler,
                                        TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<ClientOrder>> createClientOrder(CreateClientOrderDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), orderDTO);

        return requestHandler.sendRequest(request, new TypeReference<ClientOrder>() {}, order -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<ClientOrder>>> createClientOrdersInBulk(List<CreateClientOrderDTO> orderDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/create/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), orderDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<ClientOrder>>() {}, orders -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<ClientOrder>>> updateClientOrdersInBulk(List<UpdateClientOrderDTO> orderDTOs) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/update/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), orderDTOs);

        return requestHandler.sendRequest(request, new TypeReference<List<ClientOrder>>() {}, orders -> {
            cachingService.clear(); // Invalidate cache
        });
    }

    public CompletableFuture<Result<List<Integer>>> deleteClientOrderInBulk(List<Integer> orderIds) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/delete/bulk";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), orderIds);

        return requestHandler.sendRequest(request, new TypeReference<List<Integer>>() {}, ids -> {
            cachingService.clear(); // Invalidate cache
        });
    }
}
