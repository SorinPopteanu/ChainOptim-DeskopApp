package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WarehouseService {

    CompletableFuture<Optional<List<Warehouse>>> getWarehousesByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<PaginatedResults<Warehouse>>> getWarehousesByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending,
            int page,
            int itemsPerPage
    );
    CompletableFuture<Optional<Warehouse>> getWarehouseById(Integer warehouseId);
}
