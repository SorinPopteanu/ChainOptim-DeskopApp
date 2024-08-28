package org.chainoptim.desktop.core.overview.snapshot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyChainSnapshot {
    private Integer id;
    private Integer organizationId;
    private Snapshot snapshot;
}
