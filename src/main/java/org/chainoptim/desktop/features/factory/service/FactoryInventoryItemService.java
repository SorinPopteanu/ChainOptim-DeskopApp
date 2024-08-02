package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.features.factory.model.FactoryInventoryItem;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FactoryInventoryItemService {

    CompletableFuture<Result<List<FactoryInventoryItem>>> getFactoryInventoryItemsByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<FactoryInventoryItem>>> getFactoryInventoryItemsByFactoryIdAdvanced(
            Integer clientId,
            SearchParams searchParams,
            SearchMode searchMode
    );
    CompletableFuture<Result<FactoryInventoryItem>> getFactoryInventoryItemById(Integer orderId);

}
