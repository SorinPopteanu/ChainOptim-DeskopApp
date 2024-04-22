package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierService {

    CompletableFuture<Result<List<Supplier>>> getSuppliersByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<Supplier>>> getSuppliersByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    );
    CompletableFuture<Result<Supplier>> getSupplierById(Integer supplierId);
}
