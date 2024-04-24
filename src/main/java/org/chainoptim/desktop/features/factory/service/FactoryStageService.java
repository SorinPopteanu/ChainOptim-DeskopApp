package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryStageService {

    CompletableFuture<Result<FactoryStage>> getFactoryStageById(Integer factoryStageId);
}
