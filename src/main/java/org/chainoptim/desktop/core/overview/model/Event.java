package org.chainoptim.desktop.core.overview.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    private String title;
    private LocalDateTime dateTime;
    private String message;
}
