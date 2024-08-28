package org.chainoptim.desktop.features.goods.product.controller;

import org.chainoptim.desktop.features.goods.pricing.model.Pricing;
import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.features.goods.pricing.service.PricingService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.TreeMap;

public class ProductPricingController implements DataReceiver<Product> {

    // Services
    private final PricingService pricingService;

    // State
    private final FallbackManager fallbackManager;
    private Product product;
    private Pricing pricing;
    private boolean isEditing = false;

    // FXML
    @FXML
    private Label quantityLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Label pricePerUnitLabel;
    @FXML
    private Slider quantitySlider;
    @FXML
    private VBox pricingGrid;

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

            renderPricing();

            quantitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                updatePricing(newVal.floatValue());
            });
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

    private void updatePricing(float quantity) {
        quantityLabel.setText(String.valueOf(quantity));
        float totalPrice = calculatePrice(quantity);
        totalPriceLabel.setText(String.valueOf(totalPrice));
        pricePerUnitLabel.setText(String.valueOf(totalPrice / quantity));
    }

    private float calculatePrice(float quantity) {
        TreeMap<Float, Float> pricePerVolume = new TreeMap<>();
        if (this.pricing.getProductPricing().getPricePerVolume() != null) {
            pricePerVolume.putAll(this.pricing.getProductPricing().getPricePerVolume());
        }
        if (pricePerVolume.isEmpty()) {
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

    private void renderPricing() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);

        float previousQuantity = 0;
        int rowIndex = 0;

        if (this.pricing.getProductPricing().getPricePerVolume() == null) {
            return;
        }

        for (Map.Entry<Float, Float> entry : this.pricing.getProductPricing().getPricePerVolume().entrySet()) {
            String rangeLabelContent;
            if (previousQuantity == 0) {
                rangeLabelContent = String.format("0 - %.2f:", entry.getKey());
            } else {
                rangeLabelContent = String.format("%.2f - %.2f:", previousQuantity, entry.getKey());
            }

            Label rangeLabel = new Label(rangeLabelContent);
            rangeLabel.getStyleClass().add("general-label-medium-large");
            gridPane.add(rangeLabel, 0, rowIndex);

            String priceLabelContent = String.format("$%.2f per unit", entry.getValue());

            Label priceLabel = new Label(priceLabelContent);
            priceLabel.getStyleClass().add("general-label-medium-large");
            gridPane.add(priceLabel, 1, rowIndex);

            rowIndex++;
            previousQuantity = entry.getKey();
        }

        pricingGrid.getChildren().add(gridPane);
    }

    @FXML
    private void handleEditPricing() {
        System.out.println("Edit pricing");
    }
}
