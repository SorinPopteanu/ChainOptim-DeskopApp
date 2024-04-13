package org.chainoptim.desktop.features.factory.model;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;

public interface ProductionToolbarActionListener {

    void onOpenAddStageRequested();
    void onOpenUpdateStageRequested();
    void onOpenAllocationPlanRequested(AllocationPlan allocationPlan, Boolean isCurrentPlan);
    void onOpenProductionHistoryRequested();
    void onOpenAddRecordRequested();
}
