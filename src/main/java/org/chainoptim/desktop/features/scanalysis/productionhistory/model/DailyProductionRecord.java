package org.chainoptim.desktop.features.scanalysis.productionhistory.model;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyProductionRecord {
    private List<ResourceAllocation> plannedResourceAllocations;
    private List<ResourceAllocation> actualResourceAllocations;
    private float durationDays;
}
