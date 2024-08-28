package org.chainoptim.desktop.core.overview.notification.model;

import org.chainoptim.desktop.core.tenant.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationUser {

    private Integer id;
    private Notification notification;
    private User user;
    private Boolean readStatus;
}
