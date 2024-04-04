package org.chainoptim.desktop.core.settings.dto;

import org.chainoptim.desktop.core.settings.model.NotificationSettings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserSettingsDTO {

    private String userId;
    private NotificationSettings notificationSettings;
}
