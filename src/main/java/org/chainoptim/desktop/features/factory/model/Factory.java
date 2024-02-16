package org.chainoptim.desktop.features.factory.model;

import lombok.Data;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.features.location.model.Location;

import java.util.Set;

@Data
public class Factory {

    private Integer id;
    private String name;
    private Location location;
}
