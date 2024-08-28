package org.chainoptim.desktop.features.production.factory.service;

import org.chainoptim.desktop.features.production.factory.dto.FactoriesSearchDTO;
import org.chainoptim.desktop.features.production.factory.dto.FactoryOverviewDTO;
import org.chainoptim.desktop.features.production.factory.model.Factory;
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
