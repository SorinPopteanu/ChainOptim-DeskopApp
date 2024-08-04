package org.chainoptim.desktop.core.map.service;

import org.chainoptim.desktop.core.map.model.SupplyChainMap;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class SupplyChainMapServiceImpl implements SupplyChainMapService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public SupplyChainMapServiceImpl(RequestHandler requestHandler,
                                     RequestBuilder requestBuilder,
                                     TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<SupplyChainMap>> getMapByOrganizationId(Integer organizationId, boolean refresh) {
        String routeAddress = "http://localhost:8080/api/v1/supply-chain-maps/" + organizationId.toString()
                + (refresh ? "/refresh" : "");

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<SupplyChainMap>() {});
    }
}
