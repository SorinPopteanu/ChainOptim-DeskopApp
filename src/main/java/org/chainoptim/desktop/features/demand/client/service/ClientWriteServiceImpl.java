package org.chainoptim.desktop.features.demand.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.demand.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.demand.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class ClientWriteServiceImpl implements ClientWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public ClientWriteServiceImpl(RequestHandler requestHandler,
                                  RequestBuilder requestBuilder,
                                  TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<Client>> createClient(CreateClientDTO clientDTO) {
        String routeAddress = "http://localhost:8080/api/v1/clients/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), clientDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Client>() {});
    }

    public CompletableFuture<Result<Client>> updateClient(UpdateClientDTO clientDTO) {
        String routeAddress = "http://localhost:8080/api/v1/clients/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), clientDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Client>() {});
    }

    public CompletableFuture<Result<Integer>> deleteClient(Integer clientId) {
        String routeAddress = "http://localhost:8080/api/v1/clients/delete/" + clientId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();
        
        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
