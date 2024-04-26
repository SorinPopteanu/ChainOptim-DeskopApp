package org.chainoptim.desktop.features.client.service;

import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientOrdersService {

    CompletableFuture<Result<List<ClientOrder>>> getClientOrdersByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<ClientOrder>>> getClientOrdersByClientIdAdvanced(
            Integer clientId,
            SearchParams searchParams
    );

}
