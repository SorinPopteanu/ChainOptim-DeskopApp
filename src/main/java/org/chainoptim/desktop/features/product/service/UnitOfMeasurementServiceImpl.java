package org.chainoptim.desktop.features.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.product.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.dto.ProductsSearchDTO;
import org.chainoptim.desktop.features.product.dto.UpdateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.model.UnitOfMeasurement;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UnitOfMeasurementServiceImpl implements UnitOfMeasurementService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    // Fetch
    public CompletableFuture<Optional<List<UnitOfMeasurement>>> getUnitsOfMeasurementByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<UnitOfMeasurement>>empty();
                    try {
                        List<UnitOfMeasurement> units = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<UnitOfMeasurement>>() {});
                        return Optional.of(units);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<UnitOfMeasurement>>empty();
                    }
                });
    }

    // Create
    public CompletableFuture<Optional<UnitOfMeasurement>> createUnitOfMeasurement(CreateUnitOfMeasurementDTO unitDTO) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/create";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(unitDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers(HEADER_KEY, headerValue)
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<UnitOfMeasurement>empty();
                    try {
                        UnitOfMeasurement newUnit = JsonUtil.getObjectMapper().readValue(response.body(), UnitOfMeasurement.class);
                        return Optional.of(newUnit);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<UnitOfMeasurement>empty();
                    }
                });
    }

    // Update
    public CompletableFuture<Optional<UnitOfMeasurement>> updateUnitOfMeasurement(UpdateUnitOfMeasurementDTO unitDTO) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(unitDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers(HEADER_KEY, headerValue)
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<UnitOfMeasurement>empty();
                    try {
                        UnitOfMeasurement updatedUnit = JsonUtil.getObjectMapper().readValue(response.body(), UnitOfMeasurement.class);
                        return Optional.of(updatedUnit);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<UnitOfMeasurement>empty();
                    }
                });
    }

    // Delete
    public CompletableFuture<Optional<Integer>> deleteUnitOfMeasurement(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/units-of-measurement/delete/" + id.toString();

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .DELETE()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<Integer>empty();
                    return Optional.of(id);
                });
    }
}
