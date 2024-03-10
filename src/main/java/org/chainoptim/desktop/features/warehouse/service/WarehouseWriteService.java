package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WarehouseWriteService {

    CompletableFuture<Optional<Warehouse>> createWarehouse(CreateWarehouseDTO warehouseDTO);

    // Update
    CompletableFuture<Optional<Warehouse>> updateWarehouse(UpdateWarehouseDTO updateWarehouseDTO);

    // Delete
    CompletableFuture<Optional<Integer>> deleteWarehouse(Integer warehouseId);
}
