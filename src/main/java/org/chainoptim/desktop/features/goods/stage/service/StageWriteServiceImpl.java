package org.chainoptim.desktop.features.goods.stage.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.goods.stage.dto.CreateStageDTO;
import org.chainoptim.desktop.features.goods.stage.dto.UpdateStageDTO;
import org.chainoptim.desktop.features.goods.stage.model.Stage;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class StageWriteServiceImpl implements StageWriteService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public StageWriteServiceImpl(RequestHandler requestHandler,
                                 RequestBuilder requestBuilder,
                                 TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<Stage>> createStage(CreateStageDTO stageDTO) {
        String routeAddress = "http://localhost:8080/api/v1/stages/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), stageDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Stage>() {});
    }

    public CompletableFuture<Result<Stage>> updateStage(UpdateStageDTO stageDTO) {
        String routeAddress = "http://localhost:8080/api/v1/stages/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), stageDTO);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Stage>() {});
    }

    public CompletableFuture<Result<Integer>> deleteStage(Integer stageId) {
        String routeAddress = "http://localhost:8080/api/v1/stages/delete/" + stageId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
