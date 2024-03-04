package org.chainoptim.desktop.features.supplier.repository;

import org.chainoptim.desktop.features.supplier.model.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierRepository {

    CompletableFuture<Optional<List<Supplier>>> getSuppliersByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<List<Supplier>>> getSuppliersByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending
    );
    CompletableFuture<Optional<Supplier>> getSupplierById(Integer supplierId);
}
