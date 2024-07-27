package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.warehouse.model.Compartment;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
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
}
