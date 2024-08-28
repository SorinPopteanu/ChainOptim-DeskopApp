package org.chainoptim.desktop.features.production.analysis.productionhistory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryProductionHistory {

    private Integer id;
    private Integer factoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String productionHistoryJson;
    private ProductionHistory productionHistory;
}
