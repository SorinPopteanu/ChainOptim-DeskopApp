package org.chainoptim.desktop.core.user.controller;

import javafx.scene.control.*;
import org.chainoptim.desktop.core.SceneManager;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.repository.UserRepository;
import org.chainoptim.desktop.core.user.service.AuthenticationService;

import javafx.fxml.FXML;

import java.util.Optional;

public class AuthController {

//    private final UserRepository userRepository;

    @FXML
    private Label formTitle;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label passwordLabel;

    @FXML
    private void initialize() {
        formTitle.getStyleClass().add("form-title");
        usernameLabel.getStyleClass().add("form-label");
        passwordLabel.getStyleClass().add("form-label");
    }

    @FXML
    private Button logoutButton;

//    public AuthController(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    @FXML
    private void handleLogin() {
        boolean isAuthenticated = AuthenticationService.login(usernameField.getText(), passwordField.getText());

        if (isAuthenticated) {
            try {
                SceneManager.loadMainScene(); // Navigate to main app

                // Load user and organization into TenantContext
//                Optional<User> user = userRepository.getUserByUsername(usernameField.getText());
//                user.ifPresent(validUser -> {
//                    System.out.println("User email: " + validUser.getEmail());
//                    TenantContext.setCurrentUser(validUser);
//                });
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
