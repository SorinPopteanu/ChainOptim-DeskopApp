package org.chainoptim.desktop.core.user.util;

import java.util.prefs.Preferences;

/*
 * Manager of JWT Token. Uses Preferences API to store and access it securely
 */
public class TokenManager {
    private static final String JWT_KEY = "jwtToken";

    public static void saveToken(String token) {
        Preferences prefs = Preferences.userNodeForPackage(TokenManager.class);
        prefs.put(JWT_KEY, token);
    }

    public static String getToken() {
        Preferences prefs = Preferences.userNodeForPackage(TokenManager.class);
        return prefs.get(JWT_KEY, null);
    }

    public static void removeToken() {
        Preferences prefs = Preferences.userNodeForPackage(TokenManager.class);
        prefs.remove(JWT_KEY);
    }
}
