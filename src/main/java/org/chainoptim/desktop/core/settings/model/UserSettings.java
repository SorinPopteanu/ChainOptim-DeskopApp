package org.chainoptim.desktop.core.settings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettings {

    private Integer id;
    private String userId;
    private NotificationSettings notificationSettings;
}
