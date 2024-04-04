package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.features.supplier.dto.CreateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierOrdersService {

    CompletableFuture<Optional<List<SupplierOrder>>> getSupplierOrdersByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<PaginatedResults<SupplierOrder>>> getSuppliersBySupplierIdAdvanced(
            Integer supplierId,
            SearchParams searchParams
    );
    CompletableFuture<SupplierOrder> createSupplierOrder(CreateSupplierOrderDTO supplierDTO);
}
