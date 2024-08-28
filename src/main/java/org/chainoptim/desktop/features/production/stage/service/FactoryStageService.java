package org.chainoptim.desktop.features.production.stage.service;

import org.chainoptim.desktop.features.production.stage.model.FactoryStage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface FactoryStageService {

    CompletableFuture<Result<FactoryStage>> getFactoryStageById(Integer factoryStageId);
}
