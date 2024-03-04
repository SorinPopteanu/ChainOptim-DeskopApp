package org.chainoptim.desktop.core.user.controller;

import com.google.inject.Inject;
import javafx.scene.control.*;
import org.chainoptim.desktop.core.main.service.SceneManager;
import org.chainoptim.desktop.core.user.service.AuthenticationService;

import javafx.fxml.FXML;

public class AuthController {

    private final AuthenticationService authenticationService;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @Inject
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @FXML
    private void handleLogin() {
        boolean isAuthenticated = authenticationService.login(usernameField.getText(), passwordField.getText());

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

}
