package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.features.supplier.dto.CreateSupplierShipmentDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierShipmentDTO;
import org.chainoptim.desktop.features.supplier.model.SupplierShipment;
import org.chainoptim.desktop.shared.httphandling.Result;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SupplierShipmentsWriteService {

    CompletableFuture<Result<SupplierShipment>> createSupplierShipment(CreateSupplierShipmentDTO supplierDTO);

    CompletableFuture<Result<List<Integer>>> deleteSupplierShipmentInBulk(List<Integer> shipmentIds);

    CompletableFuture<Result<List<SupplierShipment>>> updateSupplierShipmentsInBulk(List<UpdateSupplierShipmentDTO> shipmentDTOs);

    CompletableFuture<Result<List<SupplierShipment>>> createSupplierShipmentsInBulk(List<CreateSupplierShipmentDTO> shipmentDTOs);
}
