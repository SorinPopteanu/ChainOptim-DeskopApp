package org.chainoptim.desktop.features.factory.repository;

import org.chainoptim.desktop.features.factory.model.Factory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryRepository {

    public CompletableFuture<Optional<List<Factory>>> getFactoriesByOrganizationId(Integer organizationId);
    public CompletableFuture<Optional<Factory>> getFactoryById(Integer factoryId);
}
