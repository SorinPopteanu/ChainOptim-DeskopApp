package org.chainoptim.desktop.features.scanalysis.productionhistory.service;

import org.chainoptim.desktop.features.scanalysis.productionhistory.dto.AddDayToFactoryProductionHistoryDTO;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryProductionHistoryService {

    CompletableFuture<Result<FactoryProductionHistory>> getFactoryProductionHistoryByFactoryId(Integer factoryId);
    CompletableFuture<Result<FactoryProductionHistory>> addDayToFactoryProductionHistory(AddDayToFactoryProductionHistoryDTO addDayDTO);
}
