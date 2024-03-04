package org.chainoptim.desktop.core.main.service;

import javafx.scene.layout.StackPane;

public interface NavigationService {

    void switchView(String viewKey);

    void setMainContentArea(StackPane contentArea);
}
