package org.chainoptim.desktop.core.main.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SidebarSubsection {
    private String name;
    private String iconPath;
    private Runnable action;
}
