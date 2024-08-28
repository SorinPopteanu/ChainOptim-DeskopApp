package org.chainoptim.desktop.core.overview.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationExtraInfo {

    private List<String> extraMessages;
}
