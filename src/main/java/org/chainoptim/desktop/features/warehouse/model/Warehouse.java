package org.chainoptim.desktop.features.warehouse.model;

import lombok.Data;
import org.chainoptim.desktop.shared.features.location.model.Location;

@Data
public class Warehouse {

    private Integer id;
    private String name;
    private Location location;
}
