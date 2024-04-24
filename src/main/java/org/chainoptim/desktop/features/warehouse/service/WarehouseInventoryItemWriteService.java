package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.warehouse.model.WarehouseInventoryItem;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface WarehouseInventoryItemWriteService {

    CompletableFuture<Result<WarehouseInventoryItem>> createWarehouseInventoryItem(CreateWarehouseInventoryItemDTO orderDTO);
    CompletableFuture<Result<WarehouseInventoryItem>> updateWarehouseInventoryItem(UpdateWarehouseInventoryItemDTO orderDTO);
    CompletableFuture<Result<Integer>> deleteWarehouseInventoryItem(Integer orderId);
}
