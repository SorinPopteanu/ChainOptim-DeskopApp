package org.chainoptim.desktop.features.warehouse.repository;

import org.chainoptim.desktop.features.warehouse.model.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WarehouseRepository {

    public CompletableFuture<Optional<List<Warehouse>>> getWarehousesByOrganizationId(Integer organizationId);
    public CompletableFuture<Optional<Warehouse>> getWarehouseById(Integer warehouseId);
}
