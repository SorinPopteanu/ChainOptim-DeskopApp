package org.chainoptim.desktop.features.scanalysis.productionhistory.dto;

import org.chainoptim.desktop.features.scanalysis.productionhistory.model.ProductionHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFactoryProductionHistoryDTO {

    private Integer factoryId;
    private ProductionHistory productionHistory;
}
