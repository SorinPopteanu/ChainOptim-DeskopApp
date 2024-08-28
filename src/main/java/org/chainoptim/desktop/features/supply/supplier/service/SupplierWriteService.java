package org.chainoptim.desktop.features.supply.supplier.service;

import org.chainoptim.desktop.features.supply.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supply.supplier.dto.UpdateSupplierDTO;
import org.chainoptim.desktop.features.supply.supplier.model.Supplier;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface SupplierWriteService {

    CompletableFuture<Result<Supplier>> createSupplier(CreateSupplierDTO supplierDTO);

    // Update
    CompletableFuture<Result<Supplier>> updateSupplier(UpdateSupplierDTO updateSupplierDTO);

    // Delete
    CompletableFuture<Result<Integer>> deleteSupplier(Integer supplierId);
}
