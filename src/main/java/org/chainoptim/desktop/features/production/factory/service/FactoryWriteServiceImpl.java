package org.chainoptim.desktop.features.production.factory.service;

import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.production.factory.dto.CreateFactoryDTO;
import org.chainoptim.desktop.features.production.factory.dto.UpdateFactoryDTO;
import org.chainoptim.desktop.features.production.factory.model.Factory;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class FactoryWriteServiceImpl implements FactoryWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public FactoryWriteServiceImpl(RequestHandler requestHandler,
                                   RequestBuilder requestBuilder,
                                   TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<Factory>> createFactory(CreateFactoryDTO factoryDTO) {
        String routeAddress = "http://localhost:8080/api/v1/factories/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), factoryDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Factory>() {});
    }

    public CompletableFuture<Result<Factory>> updateFactory(UpdateFactoryDTO factoryDTO) {
        String routeAddress = "http://localhost:8080/api/v1/factories/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), factoryDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Factory>() {});
    }

    public CompletableFuture<Result<Integer>> deleteFactory(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/v1/factories/delete/" + factoryId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
