package org.chainoptim.desktop.features.factory.repository;

import org.chainoptim.desktop.features.factory.dto.CreateFactoryDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryDTO;
import org.chainoptim.desktop.features.factory.model.Factory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryWriteRepository {

    CompletableFuture<Optional<Factory>> createFactory(CreateFactoryDTO factoryDTO);

    // Update
    CompletableFuture<Optional<Factory>> updateFactory(UpdateFactoryDTO updateFactoryDTO);

    // Delete
    CompletableFuture<Optional<Integer>> deleteFactory(Integer factoryId);
}
