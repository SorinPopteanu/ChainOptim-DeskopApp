package org.chainoptim.desktop.features.scanalysis.productgraph.service;

import org.chainoptim.desktop.features.scanalysis.productgraph.model.ProductProductionGraph;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProductProductionGraphService {

    CompletableFuture<List<ProductProductionGraph>> getProductGraphById(Integer productId);
    CompletableFuture<Optional<ProductProductionGraph>> refreshProductGraph(Integer productId);
}
