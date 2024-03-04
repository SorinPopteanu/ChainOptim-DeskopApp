package org.chainoptim.desktop.core.user.service;

import java.util.Optional;

public interface AuthenticationService {
    boolean login(String username, String password);
    boolean validateJWTToken(String jwtToken);
    Optional<String> getUsernameFromJWTToken(String jwtToken);
    void logout();
}
