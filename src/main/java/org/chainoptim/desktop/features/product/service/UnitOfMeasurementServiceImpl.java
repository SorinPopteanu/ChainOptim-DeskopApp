package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.product.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.dto.UpdateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.model.UnitOfMeasurement;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UnitOfMeasurementServiceImpl implements UnitOfMeasurementService {

    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;

    @Inject
    public UnitOfMeasurementServiceImpl(RequestBuilder requestBuilder, RequestHandler requestHandler) {
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
    }

    // Fetch
    public CompletableFuture<Result<List<UnitOfMeasurement>>> getUnitsOfMeasurementByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<UnitOfMeasurement>>() {});
    }

    // Create
    public CompletableFuture<Result<UnitOfMeasurement>> createUnitOfMeasurement(CreateUnitOfMeasurementDTO unitDTO) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, TokenManager.getToken(), unitDTO);

        return requestHandler.sendRequest(request, new TypeReference<UnitOfMeasurement>() {});
    }

    // Update
    public CompletableFuture<Result<UnitOfMeasurement>> updateUnitOfMeasurement(UpdateUnitOfMeasurementDTO unitDTO) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, TokenManager.getToken(), unitDTO);

        return requestHandler.sendRequest(request, new TypeReference<UnitOfMeasurement>() {});
    }

    // Delete
    public CompletableFuture<Result<Integer>> deleteUnitOfMeasurement(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/delete/" + id.toString();

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, TokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
