package org.chainoptim.desktop.features.production.analysis.factorygraph.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.production.analysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FactoryProductionGraphServiceImpl implements FactoryProductionGraphService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public FactoryProductionGraphServiceImpl(RequestHandler requestHandler,
                                             RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<List<FactoryProductionGraph>>> getFactoryGraphById(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/v1/factory-graphs/" + factoryId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<FactoryProductionGraph>>() {});
    }

    public CompletableFuture<Result<FactoryProductionGraph>> refreshFactoryGraph(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/v1/factory-graphs/update/" + factoryId.toString() + "/refresh";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<FactoryProductionGraph>() {});
    }
}
