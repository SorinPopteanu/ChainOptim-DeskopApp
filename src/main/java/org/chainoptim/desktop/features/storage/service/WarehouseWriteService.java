package org.chainoptim.desktop.features.storage.service;

import org.chainoptim.desktop.features.storage.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.storage.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.storage.model.Warehouse;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface WarehouseWriteService {

    CompletableFuture<Result<Warehouse>> createWarehouse(CreateWarehouseDTO warehouseDTO);

    // Update
    CompletableFuture<Result<Warehouse>> updateWarehouse(UpdateWarehouseDTO updateWarehouseDTO);

    // Delete
    CompletableFuture<Result<Integer>> deleteWarehouse(Integer warehouseId);
}
