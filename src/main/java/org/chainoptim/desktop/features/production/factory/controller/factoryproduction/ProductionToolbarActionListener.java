package org.chainoptim.desktop.features.production.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.production.analysis.resourceallocation.model.AllocationPlan;

public interface ProductionToolbarActionListener {

    void onOpenAddStageRequested();
    void onOpenUpdateStageRequested();
    void onOpenAllocationPlanRequested(AllocationPlan allocationPlan, Boolean isCurrentPlan);
    void onOpenProductionHistoryRequested();
    void onOpenAddRecordRequested();
}
