package org.chainoptim.desktop.core.user.repository;

import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<User> getUserByUsername(String username) {
        String routeAddress = "http://localhost:8080/api/users/username/" + username;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                User user = JsonUtil.getObjectMapper().readValue(responseBody, User.class);
                return Optional.of(user);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
