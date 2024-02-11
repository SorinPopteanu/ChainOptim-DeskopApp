package org.chainoptim.desktop.core.user.controller;

import org.chainoptim.desktop.core.SceneManager;
import org.chainoptim.desktop.core.user.service.AuthenticationService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button logoutButton;

    @FXML
    private void handleLogin() {
        boolean isAuthenticated = AuthenticationService.login(usernameField.getText(), passwordField.getText());

        if (isAuthenticated) {
            try {
                SceneManager.loadMainScene(); // Navigate to main app
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            // TODO: Better handling in the future
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Username or password is incorrect.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleShowSignUp() {
        // Handle show sign up
        System.out.println("Navigate to Signup window");
    }

    @FXML
    private void handleLogout() {
        AuthenticationService.logout(); // Clear JWT token from storage

        // Switch back to login scene
        try {
            SceneManager.loadLoginScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
