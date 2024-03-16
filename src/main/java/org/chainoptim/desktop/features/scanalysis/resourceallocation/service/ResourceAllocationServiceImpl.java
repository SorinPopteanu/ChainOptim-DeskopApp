package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
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

public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";


    public CompletableFuture<Optional<AllocationPlan>> allocateFactoryResources(Integer factoryId, Float duration) {
        String routeAddress = "http://localhost:8080/api/v1/factories/allocate-resources/" + factoryId;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        String jsonPayload = null;
        try {
            jsonPayload = JsonUtil.getObjectMapper().writeValueAsString(duration);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assert jsonPayload != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .header("Content-Type", "application/json") // Set Content-Type header to application/json
                .header(HEADER_KEY, headerValue)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<AllocationPlan>empty();
                    try {
                        AllocationPlan allocationPlan = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<AllocationPlan>() {});
                        return Optional.of(allocationPlan);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<AllocationPlan>empty();
                    }
                });
    }
}
