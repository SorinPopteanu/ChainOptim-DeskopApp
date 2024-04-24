package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WarehouseWriteService {

    CompletableFuture<Result<Warehouse>> createWarehouse(CreateWarehouseDTO warehouseDTO);

    // Update
    CompletableFuture<Result<Warehouse>> updateWarehouse(UpdateWarehouseDTO updateWarehouseDTO);

    // Delete
    CompletableFuture<Result<Integer>> deleteWarehouse(Integer warehouseId);
}
