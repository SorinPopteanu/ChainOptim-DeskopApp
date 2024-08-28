package org.chainoptim.desktop.features.demand.service;

import org.chainoptim.desktop.features.demand.model.ClientOrder;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientOrdersService {

    CompletableFuture<Result<List<ClientOrder>>> getClientOrdersByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<ClientOrder>>> getClientOrdersAdvanced(
            Integer clientId,
            SearchMode searchMode,
            SearchParams searchParams
    );

}
