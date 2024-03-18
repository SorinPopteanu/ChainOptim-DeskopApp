package org.chainoptim.desktop.features.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.client.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ClientOrdersServiceImpl implements ClientOrdersService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<List<ClientOrder>>> getClientOrdersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<ClientOrder>>empty();
                    try {
                        List<ClientOrder> orders = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<ClientOrder>>() {});
                        return Optional.of(orders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<ClientOrder>>empty();
                    }
                });
    }

    public CompletableFuture<ClientOrder> createClientOrder(CreateClientOrderDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/client-orders/create";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(orderDTO);
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return null;
                    try {
                        return JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<ClientOrder>() {});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }
}
