package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.features.supplier.model.SupplierShipment;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SupplierShipmentsService {

    CompletableFuture<Result<List<SupplierShipment>>> getSupplierShipmentsByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<SupplierShipment>>> getSupplierShipmentsAdvanced(
            Integer entityId,
            SearchMode searchMode,
            SearchParams searchParams
    );

}
