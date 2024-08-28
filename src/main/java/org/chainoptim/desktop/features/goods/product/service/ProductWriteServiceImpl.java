package org.chainoptim.desktop.features.goods.product.service;

import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.goods.product.dto.CreateProductDTO;
import org.chainoptim.desktop.features.goods.product.dto.UpdateProductDTO;
import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class ProductWriteServiceImpl implements ProductWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public ProductWriteServiceImpl(RequestHandler requestHandler,
                                   RequestBuilder requestBuilder,
                                   TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<Product>> createProduct(CreateProductDTO productDTO) {
        String routeAddress = "http://localhost:8080/api/v1/products/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), productDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Product>() {});
    }

    public CompletableFuture<Result<Product>> updateProduct(UpdateProductDTO productDTO) {
        String routeAddress = "http://localhost:8080/api/v1/products/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), productDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Product>() {});
    }

    public CompletableFuture<Result<Integer>> deleteProduct(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/products/delete/" + productId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
