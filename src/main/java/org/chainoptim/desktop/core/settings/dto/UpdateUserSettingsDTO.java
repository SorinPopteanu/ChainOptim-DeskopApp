package org.chainoptim.desktop.core.settings.dto;

import org.chainoptim.desktop.core.settings.model.GeneralSettings;
import org.chainoptim.desktop.core.settings.model.NotificationSettings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserSettingsDTO {

    private Integer id;
    private String userId;
    private GeneralSettings generalSettings;
    private NotificationSettings notificationSettings;
}
