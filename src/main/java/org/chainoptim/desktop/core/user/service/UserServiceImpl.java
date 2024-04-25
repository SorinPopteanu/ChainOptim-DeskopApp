package org.chainoptim.desktop.core.user.service;

import org.chainoptim.desktop.core.user.dto.AssignBasicRoleDTO;
import org.chainoptim.desktop.core.user.dto.AssignCustomRoleDTO;
import org.chainoptim.desktop.core.user.dto.UserSearchResultDTO;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserServiceImpl implements UserService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;
    private static final String BASE_PATH = "http://localhost:8080/api/v1/users";

    @Inject
    public UserServiceImpl(RequestHandler requestHandler, RequestBuilder requestBuilder, TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<User>> getUserByUsername(String username) {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String routeAddress = BASE_PATH + "/username/" + encodedUsername;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<User>() {});
    }

    public CompletableFuture<Result<List<User>>> getUsersByCustomRoleId(Integer customRoleId) {
        String routeAddress = BASE_PATH + "/search/custom-role/" + customRoleId;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<List<User>>() {});
    }

    public CompletableFuture<Result<PaginatedResults<UserSearchResultDTO>>> searchPublicUsers(String searchQuery, int page, int itemsPerPage) {
        String encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        String routeAddress = BASE_PATH + "/search/public?searchQuery=" + encodedSearchQuery + "&page=" + page + "&itemsPerPage=" + itemsPerPage;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<PaginatedResults<UserSearchResultDTO>>() {});
    }

    // Write
    public CompletableFuture<Result<User>> assignBasicRoleToUser(String userId, User.Role role) {
        String routeAddress = BASE_PATH + "/" + userId + "/assign-basic-role";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), new AssignBasicRoleDTO(role));
        if (request == null) return requestHandler.getParsingErrorResult();

        return requestHandler.sendRequest(request, new TypeReference<User>() {});
    }

    public CompletableFuture<Result<User>> assignCustomRoleToUser(String userId, Integer roleId) {
        String routeAddress = BASE_PATH + "/" + userId + "/assign-custom-role";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), new AssignCustomRoleDTO(roleId));

        return requestHandler.sendRequest(request, new TypeReference<User>() {});
    }

    public CompletableFuture<Result<User>> removeUserFromOrganization(String userId, Integer organizationId) {
        String routeAddress = BASE_PATH + "/" + userId + "/remove-from-organization/" + organizationId.toString();

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), null);

        return requestHandler.sendRequest(request, new TypeReference<User>() {});
    }
}

