package org.chainoptim.desktop.core.organization.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permissions {

    private FeaturePermissions products;
    private FeaturePermissions factories;
    private FeaturePermissions warehouses;
    private FeaturePermissions suppliers;
    private FeaturePermissions clients;
}
