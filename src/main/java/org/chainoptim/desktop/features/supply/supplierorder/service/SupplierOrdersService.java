package org.chainoptim.desktop.features.supply.supplierorder.service;

import org.chainoptim.desktop.features.supply.supplierorder.model.SupplierOrder;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SupplierOrdersService {

    CompletableFuture<Result<List<SupplierOrder>>> getSupplierOrdersByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<SupplierOrder>>> getSupplierOrdersAdvanced(
            Integer entityId,
            SearchMode searchMode,
            SearchParams searchParams
    );

}
