package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.features.supplier.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierWriteService {

    CompletableFuture<Optional<Supplier>> createSupplier(CreateSupplierDTO supplierDTO);

    // Update
    CompletableFuture<Optional<Supplier>> updateSupplier(UpdateSupplierDTO updateSupplierDTO);

    // Delete
    CompletableFuture<Optional<Integer>> deleteSupplier(Integer supplierId);
}
