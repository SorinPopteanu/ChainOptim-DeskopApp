package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.client.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientOrderDTO;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class ClientOrderWriteServiceImpl implements ClientOrderWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public ClientOrderWriteServiceImpl(RequestHandler requestHandler,
                                       RequestBuilder requestBuilder,
                                       TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<ClientOrder>> createClientOrder(CreateClientOrderDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<ClientOrder>() {});
    }

    public CompletableFuture<Result<ClientOrder>> updateClientOrder(UpdateClientOrderDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), orderDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<ClientOrder>() {});
    }

    public CompletableFuture<Result<Integer>> deleteClientOrder(Integer orderId) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/delete/" + orderId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
