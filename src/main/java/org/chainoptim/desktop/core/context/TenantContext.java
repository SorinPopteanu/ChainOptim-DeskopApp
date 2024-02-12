package org.chainoptim.desktop.core.context;

import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.user.model.User;

public class TenantContext {

    private static User currentUser;
    private static Organization currentOrganization;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        TenantContext.currentUser = currentUser;
    }

    public static Organization getCurrentOrganization() {
        return currentOrganization;
    }

    public static void setCurrentOrganization(Organization currentOrganization) {
        TenantContext.currentOrganization = currentOrganization;
    }
}
