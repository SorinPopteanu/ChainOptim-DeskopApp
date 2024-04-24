package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.features.factory.dto.CreateFactoryDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryDTO;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryWriteService {

    CompletableFuture<Result<Factory>> createFactory(CreateFactoryDTO factoryDTO);

    // Update
    CompletableFuture<Result<Factory>> updateFactory(UpdateFactoryDTO updateFactoryDTO);

    // Delete
    CompletableFuture<Result<Integer>> deleteFactory(Integer factoryId);
}
