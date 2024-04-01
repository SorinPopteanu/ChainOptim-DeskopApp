package org.chainoptim.desktop.core.overview.service;

import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SupplyChainSnapshotServiceImpl implements SupplyChainSnapshotService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<SupplyChainSnapshot>> getSupplyChainSnapshot(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/supply-chain-snapshots/organization/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<SupplyChainSnapshot>empty();
                    try {
                        SupplyChainSnapshot supplyChainSnapshot = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<SupplyChainSnapshot>() {});
                        return Optional.of(supplyChainSnapshot);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<SupplyChainSnapshot>empty();
                    }
                });
    }
}
