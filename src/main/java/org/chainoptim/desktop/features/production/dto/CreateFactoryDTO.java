package org.chainoptim.desktop.features.production.dto;

import lombok.Data;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;

@Data
public class CreateFactoryDTO {

    private String name;
    private Integer organizationId;
    private Integer locationId;
    private CreateLocationDTO location;
    private boolean createLocation;
}

