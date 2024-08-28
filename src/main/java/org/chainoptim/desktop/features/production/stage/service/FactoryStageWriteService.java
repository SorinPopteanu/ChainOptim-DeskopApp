package org.chainoptim.desktop.features.production.stage.service;

import org.chainoptim.desktop.features.production.stage.dto.CreateFactoryStageDTO;
import org.chainoptim.desktop.features.production.stage.dto.UpdateFactoryStageDTO;
import org.chainoptim.desktop.features.production.stage.model.FactoryStage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface FactoryStageWriteService {

    CompletableFuture<Result<FactoryStage>> createFactoryStage(CreateFactoryStageDTO stageDTO, Boolean refreshGraph);
    CompletableFuture<Result<FactoryStage>> updateFactoryStage(UpdateFactoryStageDTO stageDTO);
    CompletableFuture<Result<Integer>> deleteFactoryStage(Integer stageId);
}
