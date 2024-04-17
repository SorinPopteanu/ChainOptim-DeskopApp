package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.features.supplier.dto.CreateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SupplierOrdersWriteService {

    CompletableFuture<SupplierOrder> createSupplierOrder(CreateSupplierOrderDTO supplierDTO);

    CompletableFuture<List<Integer>> deleteSupplierOrderInBulk(List<Integer> orderIds);

    CompletableFuture<List<SupplierOrder>> updateSupplierOrdersInBulk(List<UpdateSupplierOrderDTO> orderDTOs);

    CompletableFuture<List<SupplierOrder>> createSupplierOrdersInBulk(List<CreateSupplierOrderDTO> orderDTOs);
}
