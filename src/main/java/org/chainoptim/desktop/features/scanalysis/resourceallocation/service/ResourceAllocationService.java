package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ResourceAllocationService {

    CompletableFuture<Optional<AllocationPlan>> allocateFactoryResources(Integer factoryId, Float duration);
}
