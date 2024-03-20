package org.chainoptim.desktop.features.productpipeline.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.productpipeline.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StageServiceImpl implements StageService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<List<StagesSearchDTO>>> getStagesByOrganizationIdSmall(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/stages/organization/" + organizationId.toString() + "/small";

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<StagesSearchDTO>>empty();
                    try {
                        List<StagesSearchDTO> stages = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<StagesSearchDTO>>() {});
                        return Optional.of(stages);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<StagesSearchDTO>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Stage>> getStageById(Integer stageId) {
        String routeAddress = "http://localhost:8080/api/v1/stages/" + stageId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<Stage>empty();
                    try {
                        Stage stage = JsonUtil.getObjectMapper().readValue(response.body(), Stage.class);
                        return Optional.of(stage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<Stage>empty();
                    }
                });
    }
}
