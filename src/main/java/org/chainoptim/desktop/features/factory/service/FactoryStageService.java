package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.features.factory.model.FactoryStage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryStageService {

    CompletableFuture<Optional<FactoryStage>> getFactoryStageById(Integer factoryStageId);
}
