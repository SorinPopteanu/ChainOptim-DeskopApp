package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ClientService {

    CompletableFuture<Result<List<Client>>> getClientsByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<Client>>> getClientsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    );
    CompletableFuture<Result<Client>> getClientById(Integer clientId);
}
