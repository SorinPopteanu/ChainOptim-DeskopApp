package org.chainoptim.desktop.features.production.analysis.resourceallocation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceAllocationPlan {

    private Integer id;
    private Integer factoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime activationDate;
    private Boolean isActive;
    private AllocationPlan allocationPlan;

}
