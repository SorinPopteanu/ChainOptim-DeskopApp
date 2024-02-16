package org.chainoptim.desktop.features.supplier.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class SupplierRepositoryImpl implements SupplierRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<List<Supplier>> getSuppliersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/suppliers/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                List<Supplier> suppliers = JsonUtil.getObjectMapper().readValue(responseBody, new TypeReference<List<Supplier>>() {});
                return Optional.of(suppliers);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Supplier> getSupplierById(Integer supplierId) {
        String routeAddress = "http://localhost:8080/api/suppliers/" + supplierId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                Supplier supplier = JsonUtil.getObjectMapper().readValue(responseBody, Supplier.class);
                return Optional.of(supplier);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
