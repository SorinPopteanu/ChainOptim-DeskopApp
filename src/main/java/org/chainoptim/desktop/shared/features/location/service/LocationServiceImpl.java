package org.chainoptim.desktop.shared.features.location.service;

import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;
import org.chainoptim.desktop.shared.features.location.dto.UpdateLocationDTO;
import org.chainoptim.desktop.shared.features.location.model.Location;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LocationServiceImpl implements LocationService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public LocationServiceImpl(RequestHandler requestHandler,
                               RequestBuilder requestBuilder,
                               TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    // Fetch
    public CompletableFuture<Result<List<Location>>> getLocationsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/locations/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<Location>>() {});
    }

    // Create
    public CompletableFuture<Result<Location>> createLocation(CreateLocationDTO locationDTO) {
        String routeAddress = "http://localhost:8080/api/v1/locations/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), locationDTO);

        return requestHandler.sendRequest(request, new TypeReference<Location>() {});
    }

    // Update
    public CompletableFuture<Result<Location>> updateLocation(UpdateLocationDTO locationDTO) {
        String routeAddress = "http://localhost:8080/api/v1/locations/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), locationDTO);

        return requestHandler.sendRequest(request, new TypeReference<Location>() {});
    }

    // Delete
    public CompletableFuture<Result<Integer>> deleteLocation(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/locations/delete/" + id.toString();

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }

}
