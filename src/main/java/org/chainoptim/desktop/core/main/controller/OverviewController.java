package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.fxml.Initializable;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.repository.UserRepository;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.util.TokenManager;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class OverviewController implements Initializable {

    private final UserRepository userRepository;

    @Inject
    public OverviewController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            String jwtToken = TokenManager.getToken();
            if (jwtToken != null) {
                Optional<String> username = AuthenticationService.getUsernameFromJWTToken(jwtToken);
                username.ifPresent(validUsername -> {
                    System.out.println("Username: " + validUsername);
                    try {
                        Optional<User> newCurrentUser = userRepository.getUserByUsername(validUsername);
                        System.out.println("Current user: " + newCurrentUser);
                        newCurrentUser.ifPresent(validUser -> {
                            System.out.println("Valid user: " + validUser.getEmail());
                            TenantContext.setCurrentUser(validUser);
                        });
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });

            }
        } else {
            System.out.println("New user exists: " + currentUser);
            Integer organizationId = currentUser.getOrganization().getId();
            System.out.println(organizationId);
                // ...
        }
    }
}
