package org.chainoptim.desktop.core.overview.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyChainSnapshot {

    private long membersCount;
    private long productCount;
    private long factoryCount;
    private long warehouseCount;
    private long suppliersCount;
    private  long clientsCount;
}
