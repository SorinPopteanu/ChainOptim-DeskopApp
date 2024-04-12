package org.chainoptim.desktop.features.scanalysis.resourceallocation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceAllocation {
    private Integer stageInputId;
    private Integer factoryStageId;
    private String stageName;
    private Integer componentId;
    private String componentName;
    private Integer allocatorInventoryItemId;
    private Float allocatedAmount;
    private Float requestedAmount;
}