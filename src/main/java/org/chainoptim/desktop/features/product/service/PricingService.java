package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.features.product.dto.CreatePricingDTO;
import org.chainoptim.desktop.features.product.dto.UpdatePricingDTO;
import org.chainoptim.desktop.features.product.model.Pricing;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface PricingService {

    CompletableFuture<Result<Pricing>> getPricingByProductId(Integer productId);
    CompletableFuture<Result<Pricing>> createPricing(CreatePricingDTO pricingDTO);
    CompletableFuture<Result<Pricing>> updatePricing(UpdatePricingDTO pricingDTO);
    CompletableFuture<Result<Void>> deletePricing(Integer productId);
}
