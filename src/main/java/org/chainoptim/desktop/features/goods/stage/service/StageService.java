package org.chainoptim.desktop.features.goods.stage.service;

import org.chainoptim.desktop.features.goods.stage.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.goods.stage.model.Stage;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StageService {

    CompletableFuture<Result<List<StagesSearchDTO>>> getStagesByOrganizationIdSmall(Integer organizationId);
    CompletableFuture<Result<PaginatedResults<StagesSearchDTO>>> getStagesByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    );
    CompletableFuture<Result<Stage>> getStageById(Integer stageId);
}
