package org.chainoptim.desktop.features.supplier.repository;

import org.chainoptim.desktop.features.supplier.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierWriteRepository {

    CompletableFuture<Optional<Supplier>> createSupplier(CreateSupplierDTO supplierDTO);

    // Update
    CompletableFuture<Optional<Supplier>> updateSupplier(UpdateSupplierDTO updateSupplierDTO);

    // Delete
    CompletableFuture<Optional<Integer>> deleteSupplier(Integer supplierId);
}
