package org.chainoptim.desktop.features.factory.service;

import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FactoryStageServiceImpl implements FactoryStageService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;

    @Inject
    public FactoryStageServiceImpl(RequestHandler requestHandler,
                                   RequestBuilder requestBuilder) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
    }

    public CompletableFuture<Result<FactoryStage>> getFactoryStageById(Integer factoryStageId) {
        String routeAddress = "http://localhost:8080/api/v1/factory-stages/" + factoryStageId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, TokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<FactoryStage>() {});
    }
}
