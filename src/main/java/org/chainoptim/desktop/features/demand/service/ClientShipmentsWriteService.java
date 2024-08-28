package org.chainoptim.desktop.features.demand.service;

import org.chainoptim.desktop.features.demand.dto.CreateClientShipmentDTO;
import org.chainoptim.desktop.features.demand.dto.UpdateClientShipmentDTO;
import org.chainoptim.desktop.features.demand.model.ClientShipment;
import org.chainoptim.desktop.shared.httphandling.Result;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientShipmentsWriteService {

    CompletableFuture<Result<ClientShipment>> createClientShipment(CreateClientShipmentDTO clientDTO);

    CompletableFuture<Result<List<Integer>>> deleteClientShipmentInBulk(List<Integer> shipmentIds);

    CompletableFuture<Result<List<ClientShipment>>> updateClientShipmentsInBulk(List<UpdateClientShipmentDTO> shipmentDTOs);

    CompletableFuture<Result<List<ClientShipment>>> createClientShipmentsInBulk(List<CreateClientShipmentDTO> shipmentDTOs);
}
