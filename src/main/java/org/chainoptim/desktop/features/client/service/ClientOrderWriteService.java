package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientOrderDTO;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface ClientOrderWriteService {

    CompletableFuture<Result<ClientOrder>> createClientOrder(CreateClientOrderDTO orderDTO);
    CompletableFuture<Result<ClientOrder>> updateClientOrder(UpdateClientOrderDTO orderDTO);
    CompletableFuture<Result<Integer>> deleteClientOrder(Integer orderId);
}
