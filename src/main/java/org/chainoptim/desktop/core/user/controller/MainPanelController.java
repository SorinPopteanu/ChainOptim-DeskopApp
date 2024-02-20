package org.chainoptim.desktop.core.user.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.chainoptim.desktop.core.context.TenantContext;

import java.net.URL;
import java.util.ResourceBundle;

public class MainPanelController implements Initializable {

    @FXML
    Label usernameLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Listen to TenantContext
        TenantContext.currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                System.out.println(newUser.getUsername());
                usernameLabel.setText(newUser.getUsername());
            } else {
                System.out.println("No user");
            }
        });

        if (TenantContext.getCurrentUser() != null) {
            usernameLabel.setText(TenantContext.getCurrentUser().getUsername());
        }
    }
}
