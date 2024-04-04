package org.chainoptim.desktop.core.context;

import org.chainoptim.desktop.core.settings.model.UserSettings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/*
 * Context for holding tenant's data across the app
 *
 */
public class TenantSettingsContext {

    private static final ObjectProperty<UserSettings> currentUserSettings = new SimpleObjectProperty<>();
    public static ObjectProperty<UserSettings> currentUserSettingsProperty() {
        return currentUserSettings;
    }

    public static UserSettings getCurrentUserSettings() {
        return currentUserSettings.get();
    }
    public static void setCurrentUserSettings(UserSettings userSettings) {
        currentUserSettings.set(userSettings);
    }
}
