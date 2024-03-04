package org.chainoptim.desktop.features.factory.repository;

import org.chainoptim.desktop.features.factory.model.Factory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryRepository {

    CompletableFuture<Optional<List<Factory>>> getFactoriesByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<List<Factory>>> getFactoriesByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending
    );
    CompletableFuture<Optional<Factory>> getFactoryById(Integer factoryId);
}
