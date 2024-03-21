package org.chainoptim.desktop.features.scanalysis.productgraph.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.scanalysis.productgraph.model.ProductProductionGraph;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ProductProductionGraphServiceImpl implements ProductProductionGraphService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<List<ProductProductionGraph>> getProductGraphById(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/product-graphs/" + productId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return new ArrayList<>();
                    try {
                        return JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<ProductProductionGraph>>() {});
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return new ArrayList<>();
                    }
                });
    }

    public CompletableFuture<Optional<ProductProductionGraph>> refreshProductGraph(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/product-graphs/update/" + productId.toString() + "/refresh";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        return Optional.of(JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<ProductProductionGraph>() {}));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
