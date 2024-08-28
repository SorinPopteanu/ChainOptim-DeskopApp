package org.chainoptim.desktop.features.goods.product.model;

import org.chainoptim.desktop.features.scanalysis.productgraph.model.ProductProductionGraph;

public interface TabsActionListener {

    void onAddStage(ProductProductionGraph productGraph);
    void onUpdateStage(ProductProductionGraph productGraph);
}
