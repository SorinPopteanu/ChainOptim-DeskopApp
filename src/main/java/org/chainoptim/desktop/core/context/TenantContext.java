package org.chainoptim.desktop.core.context;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.user.model.User;

/*
 * Context for holding tenant's data across the app
 *
 */
public class TenantContext {

    @Getter @Setter
    private static boolean isLoggedIn = false;

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
