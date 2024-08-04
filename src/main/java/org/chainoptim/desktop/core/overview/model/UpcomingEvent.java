package org.chainoptim.desktop.core.overview.model;

import org.chainoptim.desktop.shared.enums.Feature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpcomingEvent {

    private Integer id;
    private Integer organizationId;
    private String title;
    private String message;
    private LocalDateTime dateTime;
    private Integer associatedEntityId;
    private Feature associatedEntityType;

}
