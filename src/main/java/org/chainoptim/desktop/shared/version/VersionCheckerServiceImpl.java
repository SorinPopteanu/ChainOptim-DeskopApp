package org.chainoptim.desktop.shared.version;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class VersionCheckerServiceImpl implements VersionCheckerService {

    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public VersionCheckerServiceImpl(RequestBuilder requestBuilder,
                                     RequestHandler requestHandler,
                                     TokenManager tokenManager) {
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<CheckVersionResponse>> checkVersion(String currentVersion) {
        String routeAddress = "http://localhost:8080/api/v1/desktop-versions/check-version?currentVersion=" + currentVersion;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<CheckVersionResponse>() {});
    }
}
