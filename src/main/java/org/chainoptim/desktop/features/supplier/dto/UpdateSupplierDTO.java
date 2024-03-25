package org.chainoptim.desktop.features.supplier.dto;

import lombok.Data;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;

@Data
public class UpdateSupplierDTO {

    private Integer id;
    private String name;
    private Integer locationId;
    private CreateLocationDTO location;
    private boolean createLocation;
}
