package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.util.TokenManager;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ResourceBundle;

public class OverviewController implements Initializable {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Inject
    public OverviewController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser != null) return;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return; // Future: Switch to Login Scene

        authenticationService.getUsernameFromJWTToken(jwtToken).ifPresent(this::fetchAndSetUser);
    }

    private void fetchAndSetUser(String username) {
        userService.getUserByUsername(username)
                .thenAcceptAsync(userOptional -> userOptional.ifPresentOrElse(this::updateCurrentUser,
                        () -> Platform.runLater(() -> System.err.println("User not found."))))
                .exceptionally(ex -> {
                    Platform.runLater(() -> System.err.println("Failed to load user: " + ex.getMessage()));
                    return null;
                });
    }

    private void updateCurrentUser(User user) {
        Platform.runLater(() -> {
            System.out.println("User found: " + user.getEmail());
            TenantContext.setCurrentUser(user);
        });
    }
}
