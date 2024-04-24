package org.chainoptim.desktop.features.productpipeline.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.productpipeline.dto.CreateStageDTO;
import org.chainoptim.desktop.features.productpipeline.dto.UpdateStageDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StageWriteServiceImpl implements StageWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public StageWriteServiceImpl(RequestHandler requestHandler,
                                 RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<Stage>> createStage(CreateStageDTO stageDTO) {
        String routeAddress = "http://localhost:8080/api/v1/stages/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, TokenManager.getToken(), stageDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Stage>() {});
    }

    public CompletableFuture<Result<Stage>> updateStage(UpdateStageDTO stageDTO) {
        String routeAddress = "http://localhost:8080/api/v1/stages/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, TokenManager.getToken(), stageDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Stage>() {});
    }

    public CompletableFuture<Result<Integer>> deleteStage(Integer stageId) {
        String routeAddress = "http://localhost:8080/api/v1/stages/delete/" + stageId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, TokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
