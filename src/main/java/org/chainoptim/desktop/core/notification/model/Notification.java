package org.chainoptim.desktop.core.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    private Integer id;
    private String title;
    private Integer entityId;
    private String entityType;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean readStatus;
    private String type;

}
