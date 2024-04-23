package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.features.factory.dto.CreateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.factory.model.FactoryInventoryItem;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface FactoryInventoryItemWriteService {

    CompletableFuture<Result<FactoryInventoryItem>> createFactoryInventoryItem(CreateFactoryInventoryItemDTO orderDTO);
    CompletableFuture<Result<FactoryInventoryItem>> updateFactoryInventoryItem(UpdateFactoryInventoryItemDTO orderDTO);
    CompletableFuture<Result<Integer>> deleteFactoryInventoryItem(Integer orderId);
}
