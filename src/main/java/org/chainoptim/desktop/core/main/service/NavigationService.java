package org.chainoptim.desktop.core.main.service;

import javafx.scene.layout.StackPane;

public interface NavigationService {

    <T> void switchView(String viewKey, boolean forward, T data);

    void setMainContentArea(StackPane contentArea);

    void goBack();

}
