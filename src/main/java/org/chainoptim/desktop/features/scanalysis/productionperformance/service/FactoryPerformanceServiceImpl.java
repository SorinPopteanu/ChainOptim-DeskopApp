package org.chainoptim.desktop.features.scanalysis.productionperformance.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.productionperformance.model.FactoryPerformance;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class FactoryPerformanceServiceImpl implements FactoryPerformanceService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public FactoryPerformanceServiceImpl(RequestHandler requestHandler,
                                         RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<FactoryPerformance>> getFactoryPerformanceByFactoryId(Integer factoryId, boolean refresh) {
        String routeAddress = "http://localhost:8080/api/v1/factory-performances/factory/" + factoryId.toString() + (refresh ? "/refresh" : "");

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<FactoryPerformance>() {});
    }
}
