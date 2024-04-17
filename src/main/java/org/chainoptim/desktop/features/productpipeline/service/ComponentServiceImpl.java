package org.chainoptim.desktop.features.productpipeline.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.productpipeline.dto.ComponentsSearchDTO;
import org.chainoptim.desktop.features.productpipeline.dto.CreateComponentDTO;
import org.chainoptim.desktop.features.productpipeline.dto.UpdateComponentDTO;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ComponentServiceImpl implements ComponentService {

    private final HttpClient client = HttpClient.newHttpClient();
    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    // Fetch
    public CompletableFuture<Optional<List<Component>>> getComponentsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/components/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Component>>empty();
                    try {
                        List<Component> components = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Component>>() {});
                        return Optional.of(components);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Component>>empty();
                    }
                });
    }

    @Override
    public CompletableFuture<Optional<List<ComponentsSearchDTO>>> getComponentsByOrganizationIdSmall(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/components/organization/" + organizationId.toString() + "/small";

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<ComponentsSearchDTO>>empty();
                    try {
                        List<ComponentsSearchDTO> components = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<ComponentsSearchDTO>>() {});
                        return Optional.of(components);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<ComponentsSearchDTO>>empty();
                    }
                });
    }

    // Create
    public CompletableFuture<Optional<Component>> createComponent(CreateComponentDTO componentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/components/create";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(componentDTO);
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
                        Component component = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Component>() {});
                        return Optional.of(component);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Component>empty();
                    }
                });
    }

    // Update
    public CompletableFuture<Optional<Component>> updateComponent(UpdateComponentDTO componentDTO) {
        String routeAddress = "http://localhost:8080/api/v1/components/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(componentDTO);
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
                        Component component = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Component>() {});
                        return Optional.of(component);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Component>empty();
                    }
                });
    }

    // Delete
    public CompletableFuture<Void> deleteComponent(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/components/delete/" + id.toString();

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .DELETE()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> null);
    }
}
