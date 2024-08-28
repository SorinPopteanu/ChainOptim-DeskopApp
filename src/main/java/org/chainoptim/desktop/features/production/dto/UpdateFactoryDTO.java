package org.chainoptim.desktop.features.production.dto;

import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;

import lombok.Data;

@Data
public class UpdateFactoryDTO {

    private Integer id;
    private String name;
    private Integer locationId;
    private CreateLocationDTO location;
    private boolean createLocation;
}
