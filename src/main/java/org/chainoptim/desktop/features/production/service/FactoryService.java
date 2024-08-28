package org.chainoptim.desktop.features.production.service;

import org.chainoptim.desktop.features.production.dto.FactoriesSearchDTO;
import org.chainoptim.desktop.features.production.dto.FactoryOverviewDTO;
import org.chainoptim.desktop.features.production.model.Factory;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FactoryService {

    CompletableFuture<Result<List<FactoriesSearchDTO>>> getFactoriesByOrganizationIdSmall(Integer organizationId);
    CompletableFuture<Result<List<Factory>>> getFactoriesByOrganizationId(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<Factory>>> getFactoriesByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    );
    CompletableFuture<Result<Factory>> getFactoryById(Integer factoryId);
    CompletableFuture<Result<FactoryOverviewDTO>> getFactoryOverview(Integer factoryId);
}
