package org.chainoptim.desktop.features.goods.pricing.service;

import org.chainoptim.desktop.features.goods.pricing.dto.CreatePricingDTO;
import org.chainoptim.desktop.features.goods.pricing.dto.UpdatePricingDTO;
import org.chainoptim.desktop.features.goods.pricing.model.Pricing;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface PricingService {

    CompletableFuture<Result<Pricing>> getPricingByProductId(Integer productId);
    CompletableFuture<Result<Pricing>> createPricing(CreatePricingDTO pricingDTO);
    CompletableFuture<Result<Pricing>> updatePricing(UpdatePricingDTO pricingDTO);
    CompletableFuture<Result<Void>> deletePricing(Integer productId);
}
