package org.chainoptim.desktop.core.user.service;

import java.util.prefs.Preferences;

public class TokenManagerImpl implements TokenManager {

    private static final String JWT_KEY = "jwtToken";
    private final Preferences prefs = Preferences.userNodeForPackage(TokenManagerImpl.class);

    @Override
    public void saveToken(String token) {
        prefs.put(JWT_KEY, token);
    }

    @Override
    public String getToken() {
        return prefs.get(JWT_KEY, null);
    }

    @Override
    public void removeToken() {
        prefs.remove(JWT_KEY);
    }
}
