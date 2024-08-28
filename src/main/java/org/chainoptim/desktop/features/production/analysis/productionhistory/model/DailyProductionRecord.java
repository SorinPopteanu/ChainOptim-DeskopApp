package org.chainoptim.desktop.features.production.analysis.productionhistory.model;

import org.chainoptim.desktop.features.production.analysis.resourceallocation.model.AllocationResult;
import org.chainoptim.desktop.features.production.analysis.resourceallocation.model.ResourceAllocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyProductionRecord {
    private List<ResourceAllocation> allocations;
    private List<AllocationResult> results;
    private float durationDays;
}
