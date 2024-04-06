package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.features.scanalysis.supply.model.SupplierPerformance;
import org.chainoptim.desktop.features.scanalysis.supply.service.SupplierPerformanceService;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;

import java.util.Optional;

public class SupplierPerformanceController implements DataReceiver<Supplier> {

    private final SupplierPerformanceService supplierPerformanceService;

    private final FallbackManager fallbackManager;

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
        // Fetch supplier performance
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
            System.out.println("Supplier performance found: " + supplierPerformance.getReport());
        });

        return supplierPerformanceOptional;
    }

    private Optional<SupplierPerformance> handlePerformanceException(Throwable ex) {
        System.out.println("Supplier performance exception: " + ex.getMessage());
        return Optional.empty();
    }






    // Metrics
    // 1. Estimated Delivery date vs Actual Delivery date
    // 2. Order Quantity vs Delivered Quantity
    // 3. Delivered Quantity / Delivery Time
    // 4. Delivered Quantity / Total Time
    // 5. Value of delivered goods (based on how needed they were)
}
