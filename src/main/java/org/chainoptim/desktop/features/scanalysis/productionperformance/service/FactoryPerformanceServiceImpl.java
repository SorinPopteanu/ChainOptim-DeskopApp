package org.chainoptim.desktop.features.scanalysis.productionperformance.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.productionperformance.model.FactoryPerformance;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FactoryPerformanceServiceImpl implements FactoryPerformanceService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<FactoryPerformance>> getFactoryPerformanceByFactoryId(Integer factoryId, boolean refresh) {
        String routeAddress = "http://localhost:8080/api/v1/factory-performances/factory/" + factoryId.toString() + (refresh ? "/refresh" : "");

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<FactoryPerformance>empty();
                    try {
                        FactoryPerformance factoryPerformance = JsonUtil.getObjectMapper().readValue(response.body(), FactoryPerformance.class);
                        return Optional.of(factoryPerformance);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<FactoryPerformance>empty();
                    }
                });
    }
}
