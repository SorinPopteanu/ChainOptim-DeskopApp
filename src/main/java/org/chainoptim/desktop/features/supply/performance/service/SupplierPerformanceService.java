package org.chainoptim.desktop.features.supply.performance.service;

import org.chainoptim.desktop.features.supply.performance.model.SupplierPerformance;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface SupplierPerformanceService {

    CompletableFuture<Result<SupplierPerformance>> getSupplierPerformanceBySupplierId(Integer supplierId, boolean refresh);
}
