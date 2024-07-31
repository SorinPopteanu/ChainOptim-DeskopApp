package org.chainoptim.desktop.features.product.controller;

import org.chainoptim.desktop.features.product.model.Pricing;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.PricingService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.util.Map;
import java.util.TreeMap;

public class ProductPricingController implements DataReceiver<Product> {

    // Services
    private final PricingService pricingService;

    // State
    private final FallbackManager fallbackManager;
    private Product product;
    private Pricing pricing;

    // FXML
    @FXML
    private Label quantityLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Label pricePerUnitLabel;
    @FXML
    private Slider quantitySlider;

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
            this.pricing = result.getData();

            quantitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                updatePricing(newVal.floatValue());
            });

        });
        return result;
    }

    private void updatePricing(float quantity) {
        quantityLabel.setText(String.valueOf(quantity));
        float totalPrice = calculatePrice(quantity);
        totalPriceLabel.setText(String.valueOf(totalPrice));
        pricePerUnitLabel.setText(String.valueOf(totalPrice / quantity));
    }

    private Result<Pricing> handlePricingError(Throwable throwable) {
        Platform.runLater(() -> {
            fallbackManager.setErrorMessage("Failed to load product pricing");
            fallbackManager.setLoading(false);
        });
        return new Result<>();
    }

    private float calculatePrice(float quantity) {
        TreeMap<Float, Float> pricePerVolume = this.pricing.getProductPricing().getPricePerVolume();
        if (pricePerVolume == null || pricePerVolume.isEmpty()) {
            return quantity * this.pricing.getProductPricing().getPricePerUnit();
        }

        // Get the highest key smaller than or equal to the given quantity
        Map.Entry<Float, Float> entry = pricePerVolume.floorEntry(quantity);
        if (entry != null) {
            float pricePerUnit = entry.getValue();
            return quantity * pricePerUnit;
        }
        return quantity * pricePerVolume.firstEntry().getValue();
    }
}
