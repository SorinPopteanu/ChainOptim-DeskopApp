package org.chainoptim.desktop.features.scanalysis.productionperformance.service;

import org.chainoptim.desktop.features.scanalysis.productionperformance.model.FactoryPerformance;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryPerformanceService {

    CompletableFuture<Result<FactoryPerformance>> getFactoryPerformanceByFactoryId(Integer factoryId, boolean refresh);
}
