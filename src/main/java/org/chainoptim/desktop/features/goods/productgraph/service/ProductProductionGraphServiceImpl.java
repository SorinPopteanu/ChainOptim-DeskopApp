package org.chainoptim.desktop.features.goods.productgraph.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.goods.productgraph.model.ProductProductionGraph;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProductProductionGraphServiceImpl implements ProductProductionGraphService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public ProductProductionGraphServiceImpl(RequestHandler requestHandler,
                                             RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<List<ProductProductionGraph>>> getProductGraphById(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/product-graphs/" + productId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<ProductProductionGraph>>() {});
    }

    public CompletableFuture<Result<ProductProductionGraph>> refreshProductGraph(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/product-graphs/update/" + productId.toString() + "/refresh";

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<ProductProductionGraph>() {});
    }
}
