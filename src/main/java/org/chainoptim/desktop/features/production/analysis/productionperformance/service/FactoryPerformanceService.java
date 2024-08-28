package org.chainoptim.desktop.features.production.analysis.productionperformance.service;

import org.chainoptim.desktop.features.production.analysis.productionperformance.model.FactoryPerformance;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface FactoryPerformanceService {

    CompletableFuture<Result<FactoryPerformance>> getFactoryPerformanceByFactoryId(Integer factoryId, boolean refresh);
}
