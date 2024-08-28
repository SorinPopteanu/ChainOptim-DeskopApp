package org.chainoptim.desktop.features.storage.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.storage.model.Crate;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CrateServiceImpl implements CrateService {

    private final RequestBuilder requestBuilder;
    private final RequestHandler requestHandler;
    private final TokenManager tokenManager;

    @Inject
    public CrateServiceImpl(RequestBuilder requestBuilder,
                            RequestHandler requestHandler,
                            TokenManager tokenManager) {
        this.requestBuilder = requestBuilder;
        this.requestHandler = requestHandler;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<Crate>>> getCratesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/crates/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<Crate>>() {});
    }
}
