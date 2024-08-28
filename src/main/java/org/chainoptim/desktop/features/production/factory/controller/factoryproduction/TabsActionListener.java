package org.chainoptim.desktop.features.production.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.production.factory.model.Factory;
import org.chainoptim.desktop.features.production.analysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.features.production.analysis.productionhistory.model.FactoryProductionHistory;

public interface TabsActionListener {

    void onAddStage(FactoryProductionGraph factoryGraph);
    void onUpdateStage(FactoryProductionGraph factoryGraph);
    void onOpenAddRecordRequested(Factory factory);
    void onAddProductionRecord(FactoryProductionHistory factoryProductionHistory);
}
