package org.chainoptim.desktop.features.production.analysis.productionhistory.dto;

import org.chainoptim.desktop.features.production.analysis.productionhistory.model.ProductionHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFactoryProductionHistoryDTO {

    private Integer id;
    private Integer factoryId;
    private ProductionHistory productionHistory;
}
