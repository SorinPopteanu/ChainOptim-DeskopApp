package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.features.scanalysis.supply.model.SupplierPerformance;
import org.chainoptim.desktop.features.scanalysis.supply.service.SupplierPerformanceService;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.TimeUtil;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Optional;

public class SupplierPerformanceController implements DataReceiver<Supplier> {

    private final SupplierPerformanceService supplierPerformanceService;

    private final FallbackManager fallbackManager;

    @FXML
    private Label totalDeliveredOrders;
    @FXML
    private Label totalDelays;
    @FXML
    private Label averageDelayPerOrder;
    @FXML
    private Label averageTimeToShipOrder;
    @FXML
    private Label onTimeOrdersPercentage;

    @Inject
    public SupplierPerformanceController(SupplierPerformanceService supplierPerformanceService,
                                         FallbackManager fallbackManager) {
        this.supplierPerformanceService = supplierPerformanceService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Supplier supplier) {
        System.out.println("Supplier received in performance: " + supplier.getName());

        loadSupplierPerformance(supplier.getId());
    }

    private void loadSupplierPerformance(Integer supplierId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierPerformanceService.getSupplierPerformanceBySupplierId(supplierId, false)
                .thenApply(this::handlePerformanceResponse)
                .exceptionally(this::handlePerformanceException);
    }

    private Optional<SupplierPerformance> handlePerformanceResponse(Optional<SupplierPerformance> supplierPerformanceOptional) {
        Platform.runLater(() -> {
            if (supplierPerformanceOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Supplier performance not found");
                return;
            }
            SupplierPerformance supplierPerformance = supplierPerformanceOptional.get();
            fallbackManager.setLoading(false);

            displayReport(supplierPerformance);
        });

        return supplierPerformanceOptional;
    }

    private Optional<SupplierPerformance> handlePerformanceException(Throwable ex) {
        System.out.println("Supplier performance exception: " + ex.getMessage());
        return Optional.empty();
    }

    private void displayReport(SupplierPerformance supplierPerformance) {
        totalDeliveredOrders.setText(Integer.toString(supplierPerformance.getReport().getTotalDeliveredOrders()));
        totalDelays.setText(TimeUtil.formatDuration(supplierPerformance.getReport().getTotalDelays()));
        averageDelayPerOrder.setText(TimeUtil.formatDuration(supplierPerformance.getReport().getAverageDelayPerOrder()));
        averageTimeToShipOrder.setText(TimeUtil.formatDuration(supplierPerformance.getReport().getAverageTimeToShipOrder()));
        onTimeOrdersPercentage.setText(supplierPerformance.getReport().getRatioOfOnTimeOrderDeliveries() * 100 + "%");
    }






    // Metrics
    // 1. Estimated Delivery date vs Actual Delivery date
    // 2. Order Quantity vs Delivered Quantity
    // 3. Delivered Quantity / Delivery Time
    // 4. Delivered Quantity / Total Time
    // 5. Value of delivered goods (based on how needed they were)
}
