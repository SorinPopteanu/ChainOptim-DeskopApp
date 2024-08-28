package org.chainoptim.desktop.features.supply.performance.service;

import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.supply.performance.model.SupplierPerformance;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class SupplierPerformanceServiceImpl implements SupplierPerformanceService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public SupplierPerformanceServiceImpl(RequestHandler requestHandler,
                                          RequestBuilder requestBuilder,
                                          TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<SupplierPerformance>> getSupplierPerformanceBySupplierId(Integer supplierId, boolean refresh) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-performance/supplier/" + supplierId.toString() + (refresh ? "/refresh" : "");

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<SupplierPerformance>() {});
    }
}
