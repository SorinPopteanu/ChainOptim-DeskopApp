package org.chainoptim.desktop.core.tenant.settings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettings {

    private Integer id;
    private String userId;
    private GeneralSettings generalSettings;
    private NotificationSettings notificationSettings;

    public UserSettings deepCopy() {
        return new UserSettings(id, userId, generalSettings.deepCopy(), notificationSettings.deepCopy());
    }
}
