package org.chainoptim.desktop.features.production.analysis.factorygraph.service;

import org.chainoptim.desktop.features.production.analysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FactoryProductionGraphService {

    CompletableFuture<Result<List<FactoryProductionGraph>>> getFactoryGraphById(Integer factoryId);
    CompletableFuture<Result<FactoryProductionGraph>> refreshFactoryGraph(Integer factoryId);
}
