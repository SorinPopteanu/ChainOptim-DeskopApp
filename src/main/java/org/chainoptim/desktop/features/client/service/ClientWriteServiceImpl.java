package org.chainoptim.desktop.features.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ClientWriteServiceImpl implements ClientWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public ClientWriteServiceImpl(RequestHandler requestHandler,
                                  RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<Client>> createClient(CreateClientDTO clientDTO) {
        String routeAddress = "http://localhost:8080/api/v1/clients/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, TokenManager.getToken(), clientDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Client>() {});
    }

    public CompletableFuture<Result<Client>> updateClient(UpdateClientDTO clientDTO) {
        String routeAddress = "http://localhost:8080/api/v1/clients/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, TokenManager.getToken(), clientDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Client>() {});
    }

    public CompletableFuture<Result<Integer>> deleteClient(Integer clientId) {
        String routeAddress = "http://localhost:8080/api/v1/clients/delete/" + clientId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, TokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();
        
        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
