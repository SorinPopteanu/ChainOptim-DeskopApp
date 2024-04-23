package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class SupplierWriteServiceImpl implements SupplierWriteService {

    private final CachingService<PaginatedResults<Supplier>> cachingService;
    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public SupplierWriteServiceImpl(CachingService<PaginatedResults<Supplier>> cachingService,
                                    RequestHandler requestHandler,
                                    RequestBuilder requestBuilder,
                                    TokenManager tokenManager) {
        this.cachingService = cachingService;
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<Supplier>> createSupplier(CreateSupplierDTO supplierDTO) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), supplierDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Supplier>() {},
                supplier -> cachingService.clear());
    }

    public CompletableFuture<Result<Supplier>> updateSupplier(UpdateSupplierDTO supplierDTO) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), supplierDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Supplier>() {},
                supplier -> cachingService.clear());
    }

    public CompletableFuture<Result<Integer>> deleteSupplier(Integer supplierId) {
        String routeAddress = "http://localhost:8080/api/v1/suppliers/delete/" + supplierId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {},
                supplier -> cachingService.clear());
    }
}
