package org.chainoptim.desktop.shared.features.location.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.product.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.dto.UpdateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.model.UnitOfMeasurement;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;
import org.chainoptim.desktop.shared.features.location.dto.UpdateLocationDTO;
import org.chainoptim.desktop.shared.features.location.model.Location;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocationServiceImpl implements LocationService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    // Fetch
    public CompletableFuture<Optional<List<Location>>> getLocationsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/locations/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Location>>empty();
                    try {
                        List<Location> locations = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Location>>() {});
                        return Optional.of(locations);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Location>>empty();
                    }
                });
    }

    // Create
    public CompletableFuture<Optional<Location>> createLocation(CreateLocationDTO locationDTO) {
        String routeAddress = "http://localhost:8080/api/v1/locations/create";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(locationDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers(HEADER_KEY, headerValue)
                .headers("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        Location location = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Location>() {});
                        return Optional.of(location);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Location>empty();
                    }
                });
    }

    // Update
    public CompletableFuture<Optional<Location>> updateLocation(UpdateLocationDTO locationDTO) {
        String routeAddress = "http://localhost:8080/api/v1/locations/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(locationDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers(HEADER_KEY, headerValue)
                .headers("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        Location location = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Location>() {});
                        return Optional.of(location);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Location>empty();
                    }
                });
    }

    // Delete
    public CompletableFuture<Optional<Integer>> deleteLocation(Integer id) {
        String routeAddress = "http://localhost:8080/api/v1/locations/delete/" + id.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    return Optional.of(id);
                });
    }

}
