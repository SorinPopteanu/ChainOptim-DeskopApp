package org.chainoptim.desktop.features.demand.clientshipment.service;

import org.chainoptim.desktop.features.demand.clientshipment.model.ClientShipment;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientShipmentsService {

    CompletableFuture<Result<List<ClientShipment>>> getClientShipmentsByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<ClientShipment>>> getClientShipmentsAdvanced(
            Integer entityId,
            SearchMode searchMode,
            SearchParams searchParams
    );

}
