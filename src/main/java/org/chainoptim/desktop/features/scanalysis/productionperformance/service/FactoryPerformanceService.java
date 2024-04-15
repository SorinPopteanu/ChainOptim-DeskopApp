package org.chainoptim.desktop.features.scanalysis.productionperformance.service;

import org.chainoptim.desktop.features.scanalysis.productionperformance.model.FactoryPerformance;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryPerformanceService {

    CompletableFuture<Optional<FactoryPerformance>> getFactoryPerformanceByFactoryId(Integer factoryId, boolean refresh);
}
