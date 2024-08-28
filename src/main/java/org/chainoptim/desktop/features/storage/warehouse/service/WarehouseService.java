package org.chainoptim.desktop.features.storage.warehouse.service;

import org.chainoptim.desktop.features.storage.warehouse.dto.WarehouseOverviewDTO;
import org.chainoptim.desktop.features.storage.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WarehouseService {

    CompletableFuture<Result<List<Warehouse>>> getWarehousesByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<Warehouse>>> getWarehousesByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    );
    CompletableFuture<Result<Warehouse>> getWarehouseById(Integer warehouseId);
    CompletableFuture<Result<WarehouseOverviewDTO>> getWarehouseOverview(Integer warehouseId);
}
