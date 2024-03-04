package org.chainoptim.desktop.features.factory.model;

import lombok.Data;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.features.location.model.Location;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class Factory {

    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private Location location;
}
