package org.chainoptim.desktop.features.goods.productgraph.service;

import org.chainoptim.desktop.features.goods.productgraph.model.ProductProductionGraph;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProductProductionGraphService {

    CompletableFuture<Result<List<ProductProductionGraph>>> getProductGraphById(Integer productId);
    CompletableFuture<Result<ProductProductionGraph>> refreshProductGraph(Integer productId);
}
