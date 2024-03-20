package org.chainoptim.desktop.features.productpipeline.service;

import org.chainoptim.desktop.features.productpipeline.dto.CreateStageDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface StageWriteService {

    CompletableFuture<Optional<Stage>> createStage(CreateStageDTO stageDTO);
}
