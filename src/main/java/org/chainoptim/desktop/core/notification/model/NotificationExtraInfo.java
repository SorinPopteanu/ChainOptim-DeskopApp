package org.chainoptim.desktop.core.notification.model;

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
