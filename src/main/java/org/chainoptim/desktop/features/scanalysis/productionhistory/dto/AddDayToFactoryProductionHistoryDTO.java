package org.chainoptim.desktop.features.scanalysis.productionhistory.dto;

import org.chainoptim.desktop.features.scanalysis.productionhistory.model.DailyProductionRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddDayToFactoryProductionHistoryDTO {

    private Integer id;
    private Integer factoryId;
    private float daysSinceStart;
    private DailyProductionRecord dailyProductionRecord;
}
