package org.chainoptim.desktop.features.productpipeline.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Component {

    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer organizationId;
    private Integer unitId;
}
