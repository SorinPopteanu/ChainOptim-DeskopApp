package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ClientService {

    CompletableFuture<Optional<List<Client>>> getClientsByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<PaginatedResults<Client>>> getClientsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    );
    CompletableFuture<Optional<Client>> getClientById(Integer clientId);
}
