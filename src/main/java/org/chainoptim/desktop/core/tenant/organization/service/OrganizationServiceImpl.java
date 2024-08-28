package org.chainoptim.desktop.core.tenant.organization.service;

import org.chainoptim.desktop.core.tenant.organization.dto.UpdateOrganizationDTO;
import org.chainoptim.desktop.core.tenant.organization.model.Organization;
import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class OrganizationServiceImpl implements OrganizationService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public OrganizationServiceImpl(RequestHandler requestHandler,
                                   RequestBuilder requestBuilder,
                                   TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<Organization>> getOrganizationById(Integer organizationId, boolean includeUsers) {
        String routeAddress = "http://localhost:8080/api/v1/organizations/" + organizationId.toString() + "?includeUsers=" + includeUsers;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<Organization>() {});
    }

    public CompletableFuture<Result<Organization>> updateOrganization(UpdateOrganizationDTO organizationDTO) {
        String routeAddress = "http://localhost:8080/api/v1/organizations/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), organizationDTO);

        return requestHandler.sendRequest(request, new TypeReference<Organization>() {});
    }
}
