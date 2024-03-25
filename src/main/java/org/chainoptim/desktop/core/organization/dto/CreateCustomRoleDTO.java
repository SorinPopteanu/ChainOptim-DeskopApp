package org.chainoptim.desktop.core.organization.dto;

import org.chainoptim.desktop.core.organization.model.Permissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomRoleDTO {

    private String name;
    private Integer organizationId;
    private Permissions permissions;
}
