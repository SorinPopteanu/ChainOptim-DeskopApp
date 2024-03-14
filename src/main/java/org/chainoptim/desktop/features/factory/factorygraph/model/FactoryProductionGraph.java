package org.chainoptim.desktop.features.factory.factorygraph.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryProductionGraph {

    private Integer id;
    private Integer factoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private FactoryGraph factoryGraph;
}

