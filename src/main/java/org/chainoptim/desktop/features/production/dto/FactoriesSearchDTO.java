package org.chainoptim.desktop.features.production.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.shared.features.location.model.Location;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FactoriesSearchDTO {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Location location;
}
