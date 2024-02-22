package org.chainoptim.desktop.core.main.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SidebarButton {
    private String name;
    private String iconPath;
    private Runnable action;
}
