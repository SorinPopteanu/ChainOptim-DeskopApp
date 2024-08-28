package org.chainoptim.desktop.core.overview.snapshot.service;

import org.chainoptim.desktop.core.overview.snapshot.model.SupplyChainSnapshot;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface SupplyChainSnapshotService {

    CompletableFuture<Result<SupplyChainSnapshot>> getSupplyChainSnapshot(Integer organizationId);
}
