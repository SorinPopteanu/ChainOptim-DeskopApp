package org.chainoptim.desktop.features.goods.pricing.service;

import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.goods.pricing.dto.CreatePricingDTO;
import org.chainoptim.desktop.features.goods.pricing.dto.UpdatePricingDTO;
import org.chainoptim.desktop.features.goods.pricing.model.Pricing;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class PricingServiceImpl implements PricingService {

    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public PricingServiceImpl(RequestBuilder requestBuilder,
                              RequestHandler requestHandler,
                              TokenManager tokenManager) {
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<Pricing>> getPricingByProductId(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/pricings/product/" + productId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<Pricing>() {});
    }

    public CompletableFuture<Result<Pricing>> createPricing(CreatePricingDTO pricingDTO) {
        String routeAddress = "http://localhost:8080/api/v1/pricings/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), pricingDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Pricing>() {});
    }

    public CompletableFuture<Result<Pricing>> updatePricing(UpdatePricingDTO pricingDTO) {
        String routeAddress = "http://localhost:8080/api/v1/pricings/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), pricingDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Pricing>() {});
    }

    public CompletableFuture<Result<Void>> deletePricing(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/pricings/delete/" + productId.toString();

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Void>() {});
    }
}
