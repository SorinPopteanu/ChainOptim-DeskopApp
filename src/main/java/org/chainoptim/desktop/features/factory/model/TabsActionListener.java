package org.chainoptim.desktop.features.factory.model;

import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;

public interface TabsActionListener {

    void onAddStage(FactoryProductionGraph factoryGraph);
    void onUpdateStage(FactoryProductionGraph factoryGraph);
    void onOpenAddRecordRequested(Factory factory);
    void onAddProductionRecord(FactoryProductionHistory factoryProductionHistory);
}
