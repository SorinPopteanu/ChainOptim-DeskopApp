package org.chainoptim.desktop.core.tenant.user.service;

public interface TokenManager {

    void saveToken(String token);
    String getToken();
    void removeToken();
}
