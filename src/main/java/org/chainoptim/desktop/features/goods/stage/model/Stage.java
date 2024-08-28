package org.chainoptim.desktop.features.goods.stage.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class Stage {

    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private Integer productId;
    private Set<StageInput> stageInputs;
    private Set<StageOutput> stageOutputs;
}
