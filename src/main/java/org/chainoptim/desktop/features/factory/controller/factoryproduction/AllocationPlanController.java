package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;

public class AllocationPlanController {

    private AllocationPlan allocationPlan;

    public void initialize(AllocationPlan allocationPlan) {
        this.allocationPlan = allocationPlan;
        System.out.println("Allocation Plan initialized: " + allocationPlan);
    }
}
