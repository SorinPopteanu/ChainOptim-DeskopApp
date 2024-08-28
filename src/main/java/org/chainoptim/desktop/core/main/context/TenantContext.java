package org.chainoptim.desktop.core.main.context;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.chainoptim.desktop.core.tenant.user.model.User;

/**
 * Context for holding tenant's data across the app
 *
 */
public class TenantContext {

    private static final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();
    public static ObjectProperty<User> currentUserProperty() {
        return currentUser;
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
}
