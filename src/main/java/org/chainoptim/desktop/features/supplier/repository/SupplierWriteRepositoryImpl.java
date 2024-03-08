package org.chainoptim.desktop.features.supplier.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SupplierWriteRepositoryImpl implements SupplierWriteRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<Optional<Supplier>> createSupplier(CreateSupplierDTO supplierDTO) {
        String routeAddress = "http://localhost:8080/api/suppliers/create";

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(supplierDTO);
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
                        Supplier supplier = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Supplier>() {});
                        return Optional.of(supplier);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Supplier>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Supplier>> updateSupplier(UpdateSupplierDTO supplierDTO) {
        String routeAddress = "http://localhost:8080/api/suppliers/update";

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(supplierDTO);
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
                        Supplier supplier = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Supplier>() {});
                        return Optional.of(supplier);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Supplier>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Integer>> deleteSupplier(Integer supplierId) {
        String routeAddress = "http://localhost:8080/api/suppliers/delete/" + supplierId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString("", StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    return Optional.of(supplierId);
                });
    }
}
