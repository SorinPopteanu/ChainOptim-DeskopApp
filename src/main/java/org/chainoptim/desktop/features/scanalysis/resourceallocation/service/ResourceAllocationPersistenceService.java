package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.dto.UpdateAllocationPlanDTO;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ResourceAllocationPersistenceService {

    CompletableFuture<Result<ResourceAllocationPlan>> getResourceAllocationPlanByFactoryId(Integer factoryId);
    CompletableFuture<Result<ResourceAllocationPlan>> updateAllocationPlan(UpdateAllocationPlanDTO allocationPlanDTO);

}
