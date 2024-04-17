package org.chainoptim.desktop.features.supplier.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SupplierOrdersWriteServiceImpl implements SupplierOrdersWriteService {

    private final HttpClient client = HttpClient.newHttpClient();
    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

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
                .thenApply (response -> null);
    }

    public CompletableFuture<List<SupplierOrder>> updateSupplierOrdersInBulk(List<UpdateSupplierOrderDTO> orderDTOs) {
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
                        return null;
                    }
                    try {
                        return JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<SupplierOrder>>() {});
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

    public CompletableFuture<List<SupplierOrder>> createSupplierOrdersInBulk(List<CreateSupplierOrderDTO> orderDTOs) {
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
                        return null;
                    }
                    try {
                        return JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<SupplierOrder>>() {});
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }
}
