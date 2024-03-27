package org.chainoptim.desktop.core.user.service;

import org.chainoptim.desktop.core.user.dto.AssignBasicRoleDTO;
import org.chainoptim.desktop.core.user.dto.AssignCustomRoleDTO;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.util.JsonUtil;
import org.chainoptim.desktop.core.user.util.TokenManager;

import com.fasterxml.jackson.core.type.TypeReference;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserServiceImpl implements UserService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<User>> getUserByUsername(String username) {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String routeAddress = "http://localhost:8080/api/v1/users/username/" + encodedUsername;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .headers(HEADER_KEY, headerValue)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                        try {
                            User user = JsonUtil.getObjectMapper().readValue(response.body(), User.class);
                            return Optional.of(user);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return Optional.empty();
                });
    }

    public CompletableFuture<Optional<List<User>>> getUsersByCustomRoleId(Integer customRoleId) {
        String routeAddress = "http://localhost:8080/api/v1/users/search/custom-role/" + customRoleId;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .headers(HEADER_KEY, headerValue)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK)
                        return Optional.<List<User>>empty();

                    try {
                        List<User> users = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<User>>() {});
                        return Optional.of(users);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return Optional.<List<User>>empty();
                });
    }

    public CompletableFuture<Optional<User>> assignBasicRoleToUser(String userId, User.Role role) {
        String routeAddress = "http://localhost:8080/api/v1/users/" + userId + "/assign-basic-role";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(new AssignBasicRoleDTO(role));
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        User user = JsonUtil.getObjectMapper().readValue(response.body(), User.class);
                        return Optional.of(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return Optional.empty();
                });
    }

    public CompletableFuture<Optional<User>> assignCustomRoleToUser(String userId, Integer roleId) {
        String routeAddress = "http://localhost:8080/api/v1/users/" + userId + "/assign-custom-role";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(new AssignCustomRoleDTO(roleId));
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        User user = JsonUtil.getObjectMapper().readValue(response.body(), User.class);
                        return Optional.of(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return Optional.empty();
                });
    }
}

