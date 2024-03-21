package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.features.factory.dto.CreateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.model.FactoryStage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryStageWriteService {

    CompletableFuture<Optional<FactoryStage>> createFactoryStage(CreateFactoryStageDTO stageDTO, Boolean refreshGraph);
    CompletableFuture<Optional<FactoryStage>> updateFactoryStage(UpdateFactoryStageDTO stageDTO);
}
