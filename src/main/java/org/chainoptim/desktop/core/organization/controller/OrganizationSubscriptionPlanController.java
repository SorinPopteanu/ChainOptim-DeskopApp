package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class OrganizationSubscriptionPlanController implements DataReceiver<Organization> {

    private Organization organization;
    @Override
    public void setData(Organization data) {
        this.organization = data;
    }
}
