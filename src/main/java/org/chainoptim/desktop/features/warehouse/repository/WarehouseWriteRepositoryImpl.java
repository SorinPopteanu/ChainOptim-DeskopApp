package org.chainoptim.desktop.features.warehouse.repository;

import com.fasterxml.jackson.core.type.TypeReference;
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

public class WarehouseWriteRepositoryImpl implements WarehouseWriteRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<Optional<Warehouse>> createWarehouse(CreateWarehouseDTO warehouseDTO) {
        String routeAddress = "http://localhost:8080/api/warehouses/create";

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
        String routeAddress = "http://localhost:8080/api/warehouses/update";

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
        String routeAddress = "http://localhost:8080/api/warehouses/delete/" + warehouseId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString("", StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    return Optional.of(warehouseId);
                });
    }
}
