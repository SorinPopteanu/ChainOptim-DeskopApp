package org.chainoptim.desktop.features.productpipeline.service;

import org.chainoptim.desktop.features.productpipeline.dto.CreateStageDTO;
import org.chainoptim.desktop.features.productpipeline.dto.UpdateStageDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface StageWriteService {

    CompletableFuture<Result<Stage>> createStage(CreateStageDTO stageDTO);
    CompletableFuture<Result<Stage>> updateStage(UpdateStageDTO stageDTO);
    CompletableFuture<Result<Integer>> deleteStage(Integer stageId);
}
