package org.chainoptim.desktop.core.organization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomRole {

    private Integer id;
    private String name;
    private Integer organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Permissions permissions;
}
