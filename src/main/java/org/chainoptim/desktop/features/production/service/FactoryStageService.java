package org.chainoptim.desktop.features.production.service;

import org.chainoptim.desktop.features.production.model.FactoryStage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface FactoryStageService {

    CompletableFuture<Result<FactoryStage>> getFactoryStageById(Integer factoryStageId);
}
