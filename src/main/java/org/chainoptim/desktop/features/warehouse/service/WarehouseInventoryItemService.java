package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.features.warehouse.model.WarehouseInventoryItem;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WarehouseInventoryItemService {

    CompletableFuture<Result<List<WarehouseInventoryItem>>> getWarehouseInventoryItemsByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<WarehouseInventoryItem>>> getWarehouseInventoryItemsByWarehouseIdAdvanced(
            Integer clientId,
            SearchParams searchParams,
            SearchMode searchMode
    );
    CompletableFuture<Result<WarehouseInventoryItem>> getWarehouseInventoryItemById(Integer orderId);

}
