package org.chainoptim.desktop.core.overview.map.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyChainMap {

    private Integer id;
    private Integer organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MapData mapData;
}
