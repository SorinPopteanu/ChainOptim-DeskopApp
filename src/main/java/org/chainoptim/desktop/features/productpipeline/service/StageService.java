package org.chainoptim.desktop.features.productpipeline.service;

import org.chainoptim.desktop.features.productpipeline.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface StageService {

    CompletableFuture<Result<List<StagesSearchDTO>>> getStagesByOrganizationIdSmall(Integer organizationId);
    CompletableFuture<Result<Stage>> getStageById(Integer stageId);
}
