package org.chainoptim.desktop.core.user.repository;

import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.util.JsonUtil;
import org.chainoptim.desktop.core.user.util.TokenManager;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserRepositoryImpl implements UserRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<Optional<User>> getUserByUsername(String username) throws UnsupportedEncodingException {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String routeAddress = "http://localhost:8080/api/users/username/" + encodedUsername;

        // Retrieve the saved JWT token
        String jwtToken = TokenManager.getToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                // Include the JWT in the Authorization header
                .header("Authorization", "Bearer " + jwtToken)
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
}

