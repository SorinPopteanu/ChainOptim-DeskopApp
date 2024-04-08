package org.chainoptim.desktop.features.scanalysis.supply.service;

import org.chainoptim.desktop.features.scanalysis.supply.model.SupplierPerformance;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierPerformanceService {

    CompletableFuture<Optional<SupplierPerformance>> getSupplierPerformanceBySupplierId(Integer supplierId, boolean refresh);
}
