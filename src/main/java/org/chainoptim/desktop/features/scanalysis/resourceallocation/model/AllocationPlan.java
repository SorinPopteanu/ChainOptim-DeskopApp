package org.chainoptim.desktop.features.scanalysis.resourceallocation.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.features.factory.factorygraph.model.FactoryGraph;
import org.chainoptim.desktop.features.factory.model.FactoryInventoryItem;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocationPlan {

    private Map<Integer, FactoryInventoryItem> inventoryBalance;

    private List<ResourceAllocation> allocations;
}