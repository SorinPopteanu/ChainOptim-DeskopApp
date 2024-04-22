package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ResourceAllocationService {

    CompletableFuture<Result<AllocationPlan>> allocateFactoryResources(Integer factoryId, Float duration);
}
