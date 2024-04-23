package org.chainoptim.desktop.features.scanalysis.supply.service;

import org.chainoptim.desktop.features.scanalysis.supply.model.SupplierPerformance;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierPerformanceService {

    CompletableFuture<Result<SupplierPerformance>> getSupplierPerformanceBySupplierId(Integer supplierId, boolean refresh);
}
