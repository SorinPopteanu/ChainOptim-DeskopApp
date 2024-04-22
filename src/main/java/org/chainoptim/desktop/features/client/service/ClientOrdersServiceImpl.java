package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.client.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientOrdersServiceImpl implements ClientOrdersService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public ClientOrdersServiceImpl(RequestHandler requestHandler,
                                   RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<List<ClientOrder>>> getClientOrdersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<List<ClientOrder>>() {});
    }

    public CompletableFuture<Result<ClientOrder>> createClientOrder(CreateClientOrderDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, TokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<ClientOrder>() {});
    }

    public CompletableFuture<Result<ClientOrder>> updateClientOrder(UpdateClientDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, TokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<ClientOrder>() {});
    }

    public CompletableFuture<Result<Integer>> deleteClientOrder(Integer orderId) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/delete/" + orderId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, TokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
