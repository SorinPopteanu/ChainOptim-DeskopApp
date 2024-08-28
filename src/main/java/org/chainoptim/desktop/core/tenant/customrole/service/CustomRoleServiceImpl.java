package org.chainoptim.desktop.core.tenant.customrole.service;

import org.chainoptim.desktop.core.tenant.customrole.dto.CreateCustomRoleDTO;
import org.chainoptim.desktop.core.tenant.customrole.dto.UpdateCustomRoleDTO;
import org.chainoptim.desktop.core.tenant.customrole.model.CustomRole;
import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CustomRoleServiceImpl implements CustomRoleService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public CustomRoleServiceImpl(RequestHandler requestHandler,
                                 RequestBuilder requestBuilder,
                                 TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<List<CustomRole>>> getCustomRolesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<CustomRole>>() {});
    }

    public CompletableFuture<Result<CustomRole>> createCustomRole(CreateCustomRoleDTO roleDTO) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/create";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.POST, routeAddress, tokenManager.getToken(), roleDTO);

        return requestHandler.sendRequest(request, new TypeReference<CustomRole>() {});
    }

    public CompletableFuture<Result<CustomRole>> updateCustomRole(UpdateCustomRoleDTO roleDTO) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), roleDTO);

        return requestHandler.sendRequest(request, new TypeReference<CustomRole>() {});
    }

    public CompletableFuture<Result<Integer>> deleteCustomRole(Integer roleId) {
        String routeAddress = "http://localhost:8080/api/v1/custom-roles/delete/" + roleId;

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.DELETE, routeAddress, tokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<Integer>() {});
    }
}
