package org.chainoptim.desktop.features.warehouse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WarehouseWriteServiceImpl implements WarehouseWriteService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<Warehouse>> createWarehouse(CreateWarehouseDTO warehouseDTO) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/create";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(warehouseDTO);
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
                        Warehouse warehouse = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Warehouse>() {});
                        return Optional.of(warehouse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Warehouse>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Warehouse>> updateWarehouse(UpdateWarehouseDTO warehouseDTO) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(warehouseDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert requestBody != null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        Warehouse warehouse = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Warehouse>() {});
                        return Optional.of(warehouse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Warehouse>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Integer>> deleteWarehouse(Integer warehouseId) {
        String routeAddress = "http://localhost:8080/api/v1/warehouses/delete/" + warehouseId;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString("", StandardCharsets.UTF_8))
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    return Optional.of(warehouseId);
                });
    }
}
