package org.chainoptim.desktop.features.demand.clientorder.service;

import org.chainoptim.desktop.features.demand.clientorder.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.demand.clientorder.dto.UpdateClientOrderDTO;
import org.chainoptim.desktop.features.demand.clientorder.model.ClientOrder;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientOrdersWriteService {

    CompletableFuture<Result<ClientOrder>> createClientOrder(CreateClientOrderDTO clientDTO);

    CompletableFuture<Result<List<Integer>>> deleteClientOrderInBulk(List<Integer> orderIds);

    CompletableFuture<Result<List<ClientOrder>>> updateClientOrdersInBulk(List<UpdateClientOrderDTO> orderDTOs);

    CompletableFuture<Result<List<ClientOrder>>> createClientOrdersInBulk(List<CreateClientOrderDTO> orderDTOs);
}
