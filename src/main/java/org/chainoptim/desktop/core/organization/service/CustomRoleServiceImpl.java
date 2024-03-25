package org.chainoptim.desktop.core.organization.service;

import org.chainoptim.desktop.core.organization.dto.CreateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.dto.UpdateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CustomRoleServiceImpl implements CustomRoleService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<List<CustomRole>>> getCustomRolesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<CustomRole>>empty();
                    try {
                        List<CustomRole> customRoles = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<CustomRole>>() {});
                        return Optional.of(customRoles);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<CustomRole>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<CustomRole>> createCustomRole(CreateCustomRoleDTO roleDTO) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/create";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(roleDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .headers(HEADER_KEY, headerValue)
                .headers("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        CustomRole customRole = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<CustomRole>() {});
                        return Optional.of(customRole);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<CustomRole>empty();
                    }
                });
    }

    public CompletableFuture<Optional<CustomRole>> updateCustomRole(UpdateCustomRoleDTO roleDTO) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(roleDTO);
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
                        CustomRole customRole = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<CustomRole>() {});
                        return Optional.of(customRole);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<CustomRole>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Integer>> deleteCustomRole(Integer roleId) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/delete/" + roleId;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .DELETE()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    return Optional.of(roleId);
                });
    }
}
