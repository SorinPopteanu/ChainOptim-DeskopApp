package org.chainoptim.desktop.core.main.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SidebarSection {
    private String name;
    private String iconPath;
    private Runnable action;

    private List<SidebarSubsection> subsections;
    private boolean isExpanded;
}
