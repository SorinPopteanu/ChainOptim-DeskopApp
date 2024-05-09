package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.dto.CreateClientShipmentDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientShipmentDTO;
import org.chainoptim.desktop.features.client.model.ClientShipment;
import org.chainoptim.desktop.shared.httphandling.Result;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientShipmentsWriteService {

    CompletableFuture<Result<ClientShipment>> createClientShipment(CreateClientShipmentDTO clientDTO);

    CompletableFuture<Result<List<Integer>>> deleteClientShipmentInBulk(List<Integer> shipmentIds);

    CompletableFuture<Result<List<ClientShipment>>> updateClientShipmentsInBulk(List<UpdateClientShipmentDTO> shipmentDTOs);

    CompletableFuture<Result<List<ClientShipment>>> createClientShipmentsInBulk(List<CreateClientShipmentDTO> shipmentDTOs);
}
