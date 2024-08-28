package org.chainoptim.desktop.core.tenant.settings.service;

import org.chainoptim.desktop.core.tenant.settings.dto.UpdateUserSettingsDTO;
import org.chainoptim.desktop.core.tenant.settings.model.UserSettings;
import org.chainoptim.desktop.core.tenant.settings.model.UserSettings;
import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class UserSettingsServiceImpl implements UserSettingsService {

    private final RequestHandler requestHandler;
    private final RequestBuilder requestBuilder;
    private final TokenManager tokenManager;

    @Inject
    public UserSettingsServiceImpl(RequestHandler requestHandler,
                                   RequestBuilder requestBuilder,
                                   TokenManager tokenManager) {
        this.requestHandler = requestHandler;
        this.requestBuilder = requestBuilder;
        this.tokenManager = tokenManager;
    }

    public CompletableFuture<Result<UserSettings>> getUserSettings(String userId) {
        String routeAddress = "http://localhost:8080/api/v1/user-settings/user/" + userId;

        HttpRequest request = requestBuilder.buildReadRequest(routeAddress, tokenManager.getToken());

        return requestHandler.sendRequest(request, new TypeReference<UserSettings>() {});
    }

    public CompletableFuture<Result<UserSettings>> saveUserSettings(UpdateUserSettingsDTO userSettingsDTO) {
        String routeAddress = "http://localhost:8080/api/v1/user-settings/update";

        HttpRequest request = requestBuilder.buildWriteRequest(
                HttpMethod.PUT, routeAddress, tokenManager.getToken(), userSettingsDTO);

        return requestHandler.sendRequest(request, new TypeReference<UserSettings>() {});
    }
}
