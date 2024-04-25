package org.chainoptim.desktop.core.overview.service;

import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplyChainSnapshotService {

    CompletableFuture<Result<SupplyChainSnapshot>> getSupplyChainSnapshot(Integer organizationId);
}
