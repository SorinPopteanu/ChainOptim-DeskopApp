package org.chainoptim.desktop.core.main.service;

import javafx.scene.layout.StackPane;

public interface NavigationService {

    void switchView(String viewKey, boolean forward);

    void setMainContentArea(StackPane contentArea);

    void goBack();
}
