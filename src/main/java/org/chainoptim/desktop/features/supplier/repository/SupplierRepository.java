package org.chainoptim.desktop.features.supplier.repository;

import org.chainoptim.desktop.features.supplier.model.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierRepository {

    public CompletableFuture<Optional<List<Supplier>>> getSuppliersByOrganizationId(Integer organizationId);
    public CompletableFuture<Optional<Supplier>> getSupplierById(Integer supplierId);
}
