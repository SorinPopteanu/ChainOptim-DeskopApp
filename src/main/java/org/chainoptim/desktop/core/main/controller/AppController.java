package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import javafx.fxml.FXML;
import org.chainoptim.desktop.core.main.service.NavigationService;

import java.io.IOException;
import java.util.Map;

/*
 * Root controller managing the currently displayed main content
 * through sidebarController and navigationService
 */
public class AppController {

    @Inject
    private NavigationService navigationService;

    @FXML
    private StackPane contentArea;

    @FXML
    private final SidebarController sidebarController;

    @Inject
    public AppController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void initialize() {
        navigationService.setMainContentArea(contentArea);
        sidebarController.setNavigationService(navigationService);
    }

}
