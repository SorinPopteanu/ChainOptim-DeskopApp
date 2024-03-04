package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.scene.layout.StackPane;

import javafx.fxml.FXML;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;

/*
 * Root controller managing the currently displayed main content
 * through sidebarController and navigationService
 */
public class AppController {

    private final NavigationService navigationService;
    @FXML
    private final SidebarController sidebarController;

    @FXML
    private StackPane contentArea;

    @Inject
    public AppController(NavigationService navigationService, SidebarController sidebarController) {
        this.navigationService = navigationService;
        this.sidebarController = sidebarController;
    }

    public void initialize() {
        navigationService.setMainContentArea(contentArea);
        sidebarController.setNavigationService(navigationService);
    }

}
