package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.client.model.ClientOrder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ClientOrdersService {

    CompletableFuture<Optional<List<ClientOrder>>> getClientOrdersByOrganizationId(Integer organizationId);

    CompletableFuture<ClientOrder> createClientOrder(CreateClientOrderDTO orderDTO);
}
