package org.chainoptim.desktop.features.goods.stage.service;

import org.chainoptim.desktop.features.goods.stage.dto.CreateStageDTO;
import org.chainoptim.desktop.features.goods.stage.dto.UpdateStageDTO;
import org.chainoptim.desktop.features.goods.stage.model.Stage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface StageWriteService {

    CompletableFuture<Result<Stage>> createStage(CreateStageDTO stageDTO);
    CompletableFuture<Result<Stage>> updateStage(UpdateStageDTO stageDTO);
    CompletableFuture<Result<Integer>> deleteStage(Integer stageId);
}
