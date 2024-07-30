package org.chainoptim.desktop.features.product.controller;

import org.chainoptim.desktop.features.product.model.Pricing;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.PricingService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;

public class ProductPricingController implements DataReceiver<Product> {

    private final PricingService pricingService;

    private final FallbackManager fallbackManager;
    private Product product;

    @Inject
    public ProductPricingController(PricingService pricingService,
                                    FallbackManager fallbackManager) {
        this.pricingService = pricingService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Product product) {
        this.product = product;

        loadPricing();
    }

    private void loadPricing() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        pricingService.getPricingByProductId(product.getId())
                .thenApply(this::handlePricingResponse)
                .exceptionally(this::handlePricingError);
    }

    private Result<Pricing> handlePricingResponse(Result<Pricing> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load product pricing");
                return;
            }

            fallbackManager.setLoading(false);
            System.out.println("Pricing loaded: " + result.getData().getProductPricing().getPricePerUnit());
        });
        return result;
    }

    private Result<Pricing> handlePricingError(Throwable throwable) {
        Platform.runLater(() -> {
            fallbackManager.setErrorMessage("Failed to load product pricing");
            fallbackManager.setLoading(false);
        });
        return new Result<>();
    }
}
