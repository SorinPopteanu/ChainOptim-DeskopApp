package org.chainoptim.desktop.features.warehouse.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<List<Warehouse>> getWarehousesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/warehouses/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                List<Warehouse> warehouses = JsonUtil.getObjectMapper().readValue(responseBody, new TypeReference<List<Warehouse>>() {});
                return Optional.of(warehouses);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Warehouse> getWarehouseById(Integer warehouseId) {
        String routeAddress = "http://localhost:8080/api/warehouses/" + warehouseId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                Warehouse warehouse = JsonUtil.getObjectMapper().readValue(responseBody, Warehouse.class);
                return Optional.of(warehouse);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
