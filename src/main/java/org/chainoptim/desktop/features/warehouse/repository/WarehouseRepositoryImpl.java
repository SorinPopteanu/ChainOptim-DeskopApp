package org.chainoptim.desktop.features.warehouse.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<Optional<List<Warehouse>>> getWarehousesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/warehouses/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Warehouse>>empty();
                    try {
                        List<Warehouse> warehouses = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Warehouse>>() {});
                        return Optional.of(warehouses);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Warehouse>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<Warehouse>>> getWarehousesByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending,
            int page,
            int itemsPerPage
    ) {
        String routeAddress = "http://localhost:8080/api/warehouses/organizations/advanced" + organizationId.toString()
                + "?searchQuery=" + searchQuery
                + "&sortOption=" + sortOption
                + "&ascending=" + ascending
                + "&page=" + page
                + "&itemsPerPage=" + itemsPerPage;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Warehouse>>empty();
                    try {
                        PaginatedResults<Warehouse> warehouses = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Warehouse>>() {});
                        return Optional.of(warehouses);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Warehouse>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Warehouse>> getWarehouseById(Integer warehouseId) {
        String routeAddress = "http://localhost:8080/api/warehouses/" + warehouseId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        Warehouse warehouse = JsonUtil.getObjectMapper().readValue(response.body(), Warehouse.class);
                        return Optional.of(warehouse);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
