package org.chainoptim.desktop.features.factory.repository;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryRepository {

    CompletableFuture<Optional<List<Factory>>> getFactoriesByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<PaginatedResults<Factory>>> getFactoriesByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending,
            int page,
            int itemsPerPage
    );
    CompletableFuture<Optional<Factory>> getFactoryById(Integer factoryId);
}
