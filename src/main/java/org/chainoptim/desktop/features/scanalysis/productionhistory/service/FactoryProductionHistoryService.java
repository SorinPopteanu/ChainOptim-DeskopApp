package org.chainoptim.desktop.features.scanalysis.productionhistory.service;

import org.chainoptim.desktop.features.scanalysis.productionhistory.dto.AddDayToFactoryProductionHistoryDTO;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FactoryProductionHistoryService {

    CompletableFuture<Optional<FactoryProductionHistory>> getFactoryProductionHistoryByFactoryId(Integer factoryId);
    CompletableFuture<Optional<FactoryProductionHistory>> addDayToFactoryProductionHistory(AddDayToFactoryProductionHistoryDTO addDayDTO);
}
