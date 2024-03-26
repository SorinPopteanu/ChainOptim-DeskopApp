package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.features.supplier.dto.CreateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SupplierOrdersService {

    CompletableFuture<Optional<List<SupplierOrder>>> getSupplierOrdersByOrganizationId(Integer organizationId);

    CompletableFuture<SupplierOrder> createSupplierOrder(CreateSupplierOrderDTO supplierDTO);
}
