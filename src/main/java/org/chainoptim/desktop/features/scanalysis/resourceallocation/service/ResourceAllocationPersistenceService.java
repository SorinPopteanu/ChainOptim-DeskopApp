package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.dto.UpdateAllocationPlanDTO;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ResourceAllocationPersistenceService {

    CompletableFuture<Optional<ResourceAllocationPlan>> getResourceAllocationPlanByFactoryId(Integer factoryId);
    CompletableFuture<Optional<ResourceAllocationPlan>> updateAllocationPlan(UpdateAllocationPlanDTO allocationPlanDTO);

}
