package org.chainoptim.desktop.core.organization.model;

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
