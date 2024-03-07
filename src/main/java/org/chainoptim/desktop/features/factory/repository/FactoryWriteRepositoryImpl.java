package org.chainoptim.desktop.features.factory.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.factory.dto.CreateFactoryDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryDTO;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FactoryWriteRepositoryImpl implements FactoryWriteRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<Optional<Factory>> createFactory(CreateFactoryDTO factoryDTO) {
        String routeAddress = "http://localhost:8080/api/factories/create";

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(factoryDTO);
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
                        Factory factory = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Factory>() {});
                        return Optional.of(factory);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Factory>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Factory>> updateFactory(UpdateFactoryDTO factoryDTO) {
        String routeAddress = "http://localhost:8080/api/factories/update";

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(factoryDTO);
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
                        Factory factory = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<Factory>() {});
                        return Optional.of(factory);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Factory>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Integer>> deleteFactory(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/factories/delete/" + factoryId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .POST(HttpRequest.BodyPublishers.ofString("", StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    return Optional.of(factoryId);
                });
    }
}
