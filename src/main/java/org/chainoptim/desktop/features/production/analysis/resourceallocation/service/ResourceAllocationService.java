package org.chainoptim.desktop.features.production.analysis.resourceallocation.service;

import org.chainoptim.desktop.features.production.analysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface ResourceAllocationService {

    CompletableFuture<Result<AllocationPlan>> allocateFactoryResources(Integer factoryId, Float duration);
}
