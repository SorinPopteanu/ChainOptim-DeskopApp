package org.chainoptim.desktop.features.production.analysis.productionhistory.service;

import org.chainoptim.desktop.features.production.analysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.features.production.analysis.productionhistory.dto.AddDayToFactoryProductionHistoryDTO;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface FactoryProductionHistoryService {

    CompletableFuture<Result<FactoryProductionHistory>> getFactoryProductionHistoryByFactoryId(Integer factoryId);
    CompletableFuture<Result<FactoryProductionHistory>> addDayToFactoryProductionHistory(AddDayToFactoryProductionHistoryDTO addDayDTO);
}
