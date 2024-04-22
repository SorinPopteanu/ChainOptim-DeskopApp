package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ClientOrdersService {

    CompletableFuture<Result<List<ClientOrder>>> getClientOrdersByOrganizationId(Integer organizationId);

    CompletableFuture<Result<ClientOrder>> createClientOrder(CreateClientOrderDTO orderDTO);
    CompletableFuture<Result<ClientOrder>> updateClientOrder(UpdateClientDTO orderDTO);
    CompletableFuture<Result<Integer>> deleteClientOrder(Integer orderId);
}
