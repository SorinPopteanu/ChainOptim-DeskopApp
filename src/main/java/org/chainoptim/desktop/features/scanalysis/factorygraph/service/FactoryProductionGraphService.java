package org.chainoptim.desktop.features.scanalysis.factorygraph.service;

import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryProductionGraphService {

    CompletableFuture<Result<List<FactoryProductionGraph>>> getFactoryGraphById(Integer factoryId);
    CompletableFuture<Result<FactoryProductionGraph>> refreshFactoryGraph(Integer factoryId);
}
