package org.chainoptim.desktop.core.overview.service;

import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;
import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class SupplyChainSnapshotServiceImpl implements SupplyChainSnapshotService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final org.chainoptim.desktop.core.user.service.TokenManager tokenManager;

    @Inject
    public SupplyChainSnapshotServiceImpl(
            RequestHandler requestHandler,
            RequestBuilder requestBuilder,
            TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<SupplyChainSnapshot>> getSupplyChainSnapshot(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/supply-chain-snapshots/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<SupplyChainSnapshot>() {});
    }
}
