package org.chainoptim.desktop.features.supplier.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
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

public class SupplierOrdersServiceImpl implements SupplierOrdersService {

    private final HttpClient client = HttpClient.newHttpClient();
    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer";

    public CompletableFuture<Optional<List<SupplierOrder>>> getSupplierOrdersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK)
                        return Optional.<List<SupplierOrder>>empty();
                    try {
                        List<SupplierOrder> orders = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<SupplierOrder>>() {
                        });
                        return Optional.of(orders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<SupplierOrder>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<SupplierOrder>>> getSuppliersBySupplierIdAdvanced(
            Integer supplierId,
            SearchParams searchParams
    ) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/organization/advanced/" + supplierId.toString()
                + "?searchQuery=" + searchParams.getSearchQuery()
                + "&sortBy=" + searchParams.getSortOption()
                + "&ascending=" + searchParams.getAscending()
                + "&page=" + searchParams.getPage()
                + "&itemsPerPage=" + searchParams.getItemsPerPage();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                        return Optional.<PaginatedResults<SupplierOrder>>empty();
                    }
                    try {
                        PaginatedResults<SupplierOrder> supplierOrders = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<SupplierOrder>>() {});
                        return Optional.of(supplierOrders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<SupplierOrder>>empty();
                    }
                });
    }


    public CompletableFuture<SupplierOrder> createSupplierOrder(CreateSupplierOrderDTO orderDTO) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/create";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        //Serialize DTO
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
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return null;
                    try {
                        return JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<SupplierOrder>() {});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });

    }

    public CompletableFuture<List<Integer>> deleteSupplierOrderInBulk(List<Integer> orderIds) {
        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/delete/bulk";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(orderIds);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert requestBody != null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .DELETE()
                .headers(HEADER_KEY, headerValue)
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply (response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                        System.out.println("Error deleting orders");
                    }
                    return null;
                });
    }

    @Override
    public CompletableFuture<List<SupplierOrder>> updateSupplierOrdersInBulk(List<UpdateSupplierOrderDTO> orderDTOs) {
        System.out.println("Updating SupplierOrders: " + orderDTOs);

        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/update/bulk";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + " " + jwtToken;

        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(orderDTOs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert requestBody != null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .header(HEADER_KEY, headerValue)
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                        System.out.println("Error updating orders. HTTP status code: " + response.statusCode());
                        System.out.println("Response body: " + response.body());
                        return null;
                    }
                    try {
                        List<SupplierOrder> updatedOrders = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<SupplierOrder>>() {});
                        // Print the updatedOrders to check if they contain the updated data
                        System.out.println("Updated SupplierOrders: " + updatedOrders);
                        return updatedOrders;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

    @Override
    public CompletableFuture<List<SupplierOrder>> createSupplierOrdersInBulk(List<CreateSupplierOrderDTO> orderDTOs) {
        System.out.println("Creating SupplierOrders: " + orderDTOs);

        String routeAddress = "http://localhost:8080/api/v1/supplier-orders/create/bulk";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + " " + jwtToken;

        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(orderDTOs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert requestBody != null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .header(HEADER_KEY, headerValue)
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                        System.out.println("Error creating orders. HTTP status code: " + response.statusCode());
                        System.out.println("Response body: " + response.body());
                        return null;
                    }
                    try {
                        List<SupplierOrder> createdOrders = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<SupplierOrder>>() {});
                        // Print the createdOrders to check if they contain the created data
                        System.out.println("Created SupplierOrders: " + createdOrders);
                        return createdOrders;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

}
