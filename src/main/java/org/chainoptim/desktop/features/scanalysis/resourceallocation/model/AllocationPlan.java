package org.chainoptim.desktop.features.scanalysis.resourceallocation.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryGraph;
import org.chainoptim.desktop.features.production.model.FactoryInventoryItem;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocationPlan {

    private FactoryGraph factoryGraph;

    private Map<Integer, FactoryInventoryItem> inventoryBalance;

    private List<ResourceAllocation> allocations;
    private List<AllocationResult> results;
    private Float durationDays;

    public void adjustForDuration(Float duration) {
        float durationRatio = durationDays != 0 ? duration / durationDays : 1;

        for (ResourceAllocation allocation : allocations) {
            allocation.setRequestedAmount(allocation.getRequestedAmount() * durationRatio);
            allocation.setAllocatedAmount(allocation.getAllocatedAmount() * durationRatio);
        }

        for (AllocationResult result : results) {
            result.setFullAmount(result.getFullAmount() * durationRatio);
            result.setResultedAmount(result.getResultedAmount() * durationRatio);
        }
    }
}