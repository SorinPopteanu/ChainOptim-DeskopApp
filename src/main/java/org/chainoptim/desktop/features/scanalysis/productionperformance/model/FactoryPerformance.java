package org.chainoptim.desktop.features.scanalysis.productionperformance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryPerformance {

    private Integer id;
    private Integer factoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private FactoryPerformanceReport report;

}
