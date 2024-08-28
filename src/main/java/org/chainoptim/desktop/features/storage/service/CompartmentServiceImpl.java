package org.chainoptim.desktop.features.storage.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.storage.dto.CreateCompartmentDTO;
import org.chainoptim.desktop.features.storage.dto.UpdateCompartmentDTO;
import org.chainoptim.desktop.features.storage.model.Compartment;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompartmentServiceImpl implements CompartmentService {

    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public CompartmentServiceImpl(RequestBuilder requestBuilder,
                                RequestHandler requestHandler,
                                TokenManager tokenManager) {
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<Compartment>>> getCompartmentsByWarehouseId(Integer warehouseId) {
        String routeAddress = "http://localhost:8080/api/v1/compartments/warehouse/" + warehouseId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<Compartment>>() {});
    }

    public CompletableFuture<Result<Compartment>> createCompartment(CreateCompartmentDTO compartmentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/compartments/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), compartmentDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Compartment>() {});
    }

    public CompletableFuture<Result<Compartment>> updateCompartment(UpdateCompartmentDTO compartmentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/compartments/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), compartmentDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Compartment>() {});
    }

    public CompletableFuture<Result<Integer>> deleteCompartment(Integer compartmentId) {
        String routeAddress = "http://localhost:8080/api/v1/compartments/delete/" + compartmentId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
