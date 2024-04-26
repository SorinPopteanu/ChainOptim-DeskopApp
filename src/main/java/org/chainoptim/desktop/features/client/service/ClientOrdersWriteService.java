package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientOrderDTO;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientOrdersWriteService {

    CompletableFuture<Result<ClientOrder>> createClientOrder(CreateClientOrderDTO clientDTO);

    CompletableFuture<Result<List<Integer>>> deleteClientOrderInBulk(List<Integer> orderIds);

    CompletableFuture<Result<List<ClientOrder>>> updateClientOrdersInBulk(List<UpdateClientOrderDTO> orderDTOs);

    CompletableFuture<Result<List<ClientOrder>>> createClientOrdersInBulk(List<CreateClientOrderDTO> orderDTOs);
}
