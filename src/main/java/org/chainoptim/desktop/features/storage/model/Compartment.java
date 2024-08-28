package org.chainoptim.desktop.features.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Compartment {

    private Integer id;
    private String name;
    private Integer warehouseId;
    private Integer organizationId;
    private CompartmentData data;
}
