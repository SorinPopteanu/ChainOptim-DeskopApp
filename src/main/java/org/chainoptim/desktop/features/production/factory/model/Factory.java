package org.chainoptim.desktop.features.production.factory.model;

import lombok.Data;
import org.chainoptim.desktop.features.production.stage.model.FactoryStage;
import org.chainoptim.desktop.shared.features.location.model.Location;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class Factory {

    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private Location location;
    private Float overallScore;
    private Float resourceDistributionScore;
    private Float resourceReadinessScore;
    private Float resourceUtilizationScore;
    private Set<FactoryStage> factoryStages;
}
