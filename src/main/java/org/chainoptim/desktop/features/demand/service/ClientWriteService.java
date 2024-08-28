package org.chainoptim.desktop.features.demand.service;

import org.chainoptim.desktop.features.demand.dto.CreateClientDTO;
import org.chainoptim.desktop.features.demand.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.demand.model.Client;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface ClientWriteService {

    CompletableFuture<Result<Client>> createClient(CreateClientDTO clientDTO);

    // Update
    CompletableFuture<Result<Client>> updateClient(UpdateClientDTO updateClientDTO);

    // Delete
    CompletableFuture<Result<Integer>> deleteClient(Integer clientId);
}
