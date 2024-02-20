package org.chainoptim.desktop.core.user.controller;

import com.google.inject.Inject;
import javafx.scene.control.*;
import org.chainoptim.desktop.core.SceneManager;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.repository.UserRepository;
import org.chainoptim.desktop.core.user.service.AuthenticationService;

import javafx.fxml.FXML;

import java.util.Optional;

public class AuthController {

    @Inject
    private UserRepository userRepository;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

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
                Optional<User> user = userRepository.getUserByUsername(usernameField.getText());
                user.ifPresent(validUser -> {
                    System.out.println("User email: " + validUser.getEmail());
                    // Update tenant context
                    TenantContext.setCurrentUser(validUser);
                });
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
