package org.chainoptim.desktop.core.tenant.organization.model;

import org.chainoptim.desktop.core.tenant.customrole.model.CustomRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationViewData {

    private Organization organization;
    private List<CustomRole> customRoles;
}
