package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.features.factory.dto.CreateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryStageWriteService {

    CompletableFuture<Result<FactoryStage>> createFactoryStage(CreateFactoryStageDTO stageDTO, Boolean refreshGraph);
    CompletableFuture<Result<FactoryStage>> updateFactoryStage(UpdateFactoryStageDTO stageDTO);
    CompletableFuture<Result<Integer>> deleteFactoryStage(Integer stageId);
}
