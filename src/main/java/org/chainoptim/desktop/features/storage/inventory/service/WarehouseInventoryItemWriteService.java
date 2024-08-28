package org.chainoptim.desktop.features.storage.inventory.service;

import org.chainoptim.desktop.features.storage.inventory.dto.CreateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.storage.inventory.dto.UpdateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.storage.inventory.model.WarehouseInventoryItem;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WarehouseInventoryItemWriteService {

    CompletableFuture<Result<WarehouseInventoryItem>> createWarehouseInventoryItem(CreateWarehouseInventoryItemDTO orderDTO);
    CompletableFuture<Result<WarehouseInventoryItem>> updateWarehouseInventoryItem(UpdateWarehouseInventoryItemDTO orderDTO);
    CompletableFuture<Result<Integer>> deleteWarehouseInventoryItem(Integer orderId);
    CompletableFuture<Result<List<Integer>>> deleteWarehouseInventoryItemsInBulk(List<Integer> itemIds);
    CompletableFuture<Result<List<WarehouseInventoryItem>>> createWarehouseInventoryItemsInBulk(List<CreateWarehouseInventoryItemDTO> itemDTOs);
    CompletableFuture<Result<List<WarehouseInventoryItem>>> updateWarehouseInventoryItemsInBulk(List<UpdateWarehouseInventoryItemDTO> itemDTOs);
}
