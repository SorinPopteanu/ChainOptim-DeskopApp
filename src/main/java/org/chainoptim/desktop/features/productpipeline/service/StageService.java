package org.chainoptim.desktop.features.productpipeline.service;

import org.chainoptim.desktop.features.productpipeline.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface StageService {

    CompletableFuture<Optional<List<StagesSearchDTO>>> getStagesByOrganizationIdSmall(Integer organizationId);
    CompletableFuture<Optional<Stage>> getStageById(Integer stageId);
}
