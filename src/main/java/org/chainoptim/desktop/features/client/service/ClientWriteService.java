package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ClientWriteService {

    CompletableFuture<Optional<Client>> createClient(CreateClientDTO clientDTO);

    // Update
    CompletableFuture<Optional<Client>> updateClient(UpdateClientDTO updateClientDTO);

    // Delete
    CompletableFuture<Optional<Integer>> deleteClient(Integer clientId);
}
