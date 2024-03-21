package org.chainoptim.desktop.features.supplier.dto;

import lombok.Data;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;

@Data
public class CreateSupplierDTO {

    private String name;
    private Integer organizationId;
    private Integer locationId;
    private CreateLocationDTO location;
    private boolean createLocation;
}

