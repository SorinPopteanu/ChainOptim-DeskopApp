package org.chainoptim.desktop.core.settings.service;

import org.chainoptim.desktop.core.settings.dto.UpdateUserSettingsDTO;
import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserSettingsServiceImpl implements UserSettingsService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<UserSettings>> getUserSettings(String userId) {
        String routeAddress = "http://localhost:8080/api/v1/user-settings/user/" + userId;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<UserSettings>empty();
                    try {
                        UserSettings userSettings = JsonUtil.getObjectMapper().readValue(response.body(), UserSettings.class);
                        return Optional.of(userSettings);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<UserSettings>empty();
                    }
                });
    }

    public CompletableFuture<Optional<UserSettings>> saveUserSettings(UpdateUserSettingsDTO userSettingsDTO) {
        String routeAddress = "http://localhost:8080/api/v1/user-settings/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(userSettingsDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .headers(HEADER_KEY, headerValue)
                .headers("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<UserSettings>empty();
                    try {
                        UserSettings userSettings = JsonUtil.getObjectMapper().readValue(response.body(), UserSettings.class);
                        return Optional.of(userSettings);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<UserSettings>empty();
                    }
                });
    }
}
