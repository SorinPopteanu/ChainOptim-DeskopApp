package org.chainoptim.desktop.features.client.dto;

import lombok.Data;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;

@Data
public class CreateClientDTO {

    private String name;
    private Integer organizationId;
    private Integer locationId;
    private CreateLocationDTO location;
    private boolean createLocation;
}

