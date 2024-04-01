package org.chainoptim.desktop.core.overview.service;

import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplyChainSnapshotService {

    CompletableFuture<Optional<SupplyChainSnapshot>> getSupplyChainSnapshot(Integer organizationId);
}
