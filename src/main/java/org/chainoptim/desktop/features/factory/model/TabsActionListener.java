package org.chainoptim.desktop.features.factory.model;

import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryProductionGraph;

public interface TabsActionListener {

    void onAddStage(FactoryProductionGraph factoryGraph);
    void onUpdateStage(FactoryProductionGraph factoryGraph);
    void onAddProductionRecord(Factory factory);
}
