package org.chainoptim.desktop.features.scanalysis.productionhistory.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.productionhistory.dto.AddDayToFactoryProductionHistoryDTO;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.features.scanalysis.supply.model.SupplierPerformance;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FactoryProductionHistoryServiceImpl implements FactoryProductionHistoryService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<FactoryProductionHistory>> getFactoryProductionHistoryByFactoryId(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/v1/factory-production-histories/factory/" + factoryId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<FactoryProductionHistory>empty();
                    try {
                        FactoryProductionHistory productionHistory = JsonUtil.getObjectMapper().readValue(response.body(), FactoryProductionHistory.class);
                        return Optional.of(productionHistory);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<FactoryProductionHistory>empty();
                    }
                });
    }

    public CompletableFuture<Optional<FactoryProductionHistory>> addDayToFactoryProductionHistory(AddDayToFactoryProductionHistoryDTO addDayDTO) {
        String routeAddress = "http://localhost:8080/api/v1/factory-production-histories/add-day";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        // Serialize DTO
        String requestBody = null;
        try {
            requestBody = JsonUtil.getObjectMapper().writeValueAsString(addDayDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert requestBody != null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers(HEADER_KEY, headerValue)
                .headers("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        FactoryProductionHistory productionHistory = JsonUtil.getObjectMapper().readValue(response.body(), FactoryProductionHistory.class);
                        return Optional.of(productionHistory);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<FactoryProductionHistory>empty();
                    }
                });
    }
}
