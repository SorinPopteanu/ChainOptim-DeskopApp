package org.chainoptim.desktop.features.scanalysis.resourceallocation.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.dto.UpdateAllocationPlanDTO;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ResourceAllocationPersistenceServiceImpl implements ResourceAllocationPersistenceService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<ResourceAllocationPlan>> getResourceAllocationPlanByFactoryId(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/v1/active-resource-allocation-plans/factory/" + factoryId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<ResourceAllocationPlan>empty();
                    try {
                        ResourceAllocationPlan allocationPlan = JsonUtil.getObjectMapper().readValue(response.body(), ResourceAllocationPlan.class);
                        return Optional.of(allocationPlan);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<ResourceAllocationPlan>empty();
                    }
                });
    }

    public CompletableFuture<Optional<ResourceAllocationPlan>> changePlanActivationStatus(UpdateAllocationPlanDTO allocationPlanDTO, boolean isActive) {
        allocationPlanDTO.setActive(isActive);
        if (isActive) {
            allocationPlanDTO.setActivationDate(LocalDateTime.now());
        }

        String routeAddress = "http://localhost:8080/api/v1/resource-allocation-plans/update";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(allocationPlanDTO);
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
                        ResourceAllocationPlan allocationPlan = JsonUtil.getObjectMapper().readValue(response.body(), ResourceAllocationPlan.class);
                        return Optional.of(allocationPlan);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<ResourceAllocationPlan>empty();
                    }
                });
    }
}
