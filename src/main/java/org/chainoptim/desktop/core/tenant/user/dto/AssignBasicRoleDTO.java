package org.chainoptim.desktop.core.tenant.user.dto;

import org.chainoptim.desktop.core.tenant.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignBasicRoleDTO {
    private User.Role role;
}
