package org.chainoptim.desktop.features.warehouse.repository;

import org.chainoptim.desktop.features.warehouse.model.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WarehouseRepository {

    CompletableFuture<Optional<List<Warehouse>>> getWarehousesByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<List<Warehouse>>> getWarehousesByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending
    );
    CompletableFuture<Optional<Warehouse>> getWarehouseById(Integer warehouseId);
}
